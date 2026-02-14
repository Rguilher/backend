package br.com.studiogui.backend.service;

import br.com.studiogui.backend.controller.dto.request.CreateAppointmentRequest;
import br.com.studiogui.backend.controller.dto.response.AppointmentDetailResponse;
import br.com.studiogui.backend.model.Appointment;
import br.com.studiogui.backend.model.SalonService;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.model.enums.AppointmentStatus;
import br.com.studiogui.backend.model.enums.UserRole;
import br.com.studiogui.backend.repository.AppointmentRepository;
import br.com.studiogui.backend.repository.SalonServiceRepository;
import br.com.studiogui.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository serviceRepository;

    // Constantes de Regra de Negócio (Em projeto grande, iriam para application.properties)
    private static final int MIN_LEAD_TIME_MINUTES = 30;
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, SalonServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<LocalTime> getAvailability(Long professionalId, Long serviceId, LocalDate date) {

        if (date.isBefore(LocalDate.now())) {
            return List.of(); // Sem agenda para o passado
        }

        int durationMinutes = 45;
        if (serviceId != null) {
            SalonService service = serviceRepository.findById(serviceId).orElse(null);
            if (service != null) {
                durationMinutes = service.getDurationMin();
            }
        }

        //  Buscar agendamentos JÁ existentes naquele dia
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59);
        List<Appointment> existingAppointments = appointmentRepository.findByProfessionalAndDate(professionalId, startOfDay, endOfDay);

        //  Gerar Slots e Filtrar
        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentSlot = OPENING_TIME;

        // Enquanto o serviço couber antes de fechar...
        while (!currentSlot.plusMinutes(durationMinutes).isAfter(CLOSING_TIME)) {

            LocalDateTime slotStart = date.atTime(currentSlot);
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

            // Regra: Não mostrar horários que já passaram hoje (ex: agora é 10h, não mostrar 08h)
            if (slotStart.isBefore(LocalDateTime.now().plusMinutes(MIN_LEAD_TIME_MINUTES))) {
                currentSlot = currentSlot.plusMinutes(durationMinutes); // Pula pro próximo
                continue;
            }

            // Verifica colisão com agendamentos existentes
            boolean isBusy = false;
            for (Appointment appointment : existingAppointments) {
                LocalDateTime appStart = appointment.getDateTime();
                LocalDateTime appEnd = appStart.plusMinutes(appointment.getService().getDurationMin());

                // Lógica de Intersecção de Horários
                if (slotStart.isBefore(appEnd) && slotEnd.isAfter(appStart)) {
                    isBusy = true;
                    break;
                }
            }

            if (!isBusy) {
                availableSlots.add(currentSlot);
            }

            // Padrão de Mercado: Agendamentos "Encaixados" (Grid System)
            // O próximo slot começa exatamente quando este termina.
            currentSlot = currentSlot.plusMinutes(durationMinutes);
        }

        return availableSlots;
    }

    @Transactional
    public AppointmentDetailResponse schedule(CreateAppointmentRequest data, Long clientId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        User professional = userRepository.findById(data.professionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        SalonService service = serviceRepository.findById(data.serviceId())
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));

        if (!service.getActive()) {
            throw new IllegalArgumentException("Este serviço não está mais disponível.");
        }

        if (professional.getRole() == UserRole.USER) {
            throw new IllegalArgumentException("O usuário selecionado não é um profissional.");
        }

        if (client.getId().equals(professional.getId())) {
            throw new IllegalArgumentException("Você não pode agendar um serviço com você mesmo.");
        }

        LocalDateTime newStart = data.startTime();
        LocalDateTime newEnd = newStart.plusMinutes(service.getDurationMin());
        if (newStart.isBefore(LocalDateTime.now().plusMinutes(MIN_LEAD_TIME_MINUTES))) {
            throw new IllegalArgumentException
                    ("O agendamento deve ser feito com no mínimo " + MIN_LEAD_TIME_MINUTES + " minutos de antecedência.");
        }
        validateBusinessHours(newStart, newEnd);

        boolean hasConflict = appointmentRepository.existsConflictingAppointment(
                professional.getId(),
                client.getId(),
                newStart,
                newEnd
        );

        if (hasConflict) {
            throw new IllegalArgumentException
                    ("Conflito de horário! O profissional ou você já possuem agendamento neste intervalo.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setDateTime(newStart);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setObservation("Agendado via App");

        appointmentRepository.save(appointment);

        return new AppointmentDetailResponse(appointment);
    }


    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));

        User userRequesting = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        boolean isAdmin = userRequesting.getRole() == UserRole.ADMIN;
        boolean isTheClient = appointment.getClient().getId().equals(userId);

        if (!isAdmin && !isTheClient) {
            throw new IllegalArgumentException("Apenas o Cliente ou a Administração podem cancelar este agendamento.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new IllegalArgumentException("Este agendamento já está cancelado.");
        }

        if (appointment.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível cancelar agendamentos passados.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    private List<AppointmentDetailResponse> listByInterval(Long userId, LocalDateTime start, LocalDateTime end) {
        List<Appointment> appointments = appointmentRepository.findByUserAndDateRange(userId, start, end);

        return appointments.stream()
                .map(AppointmentDetailResponse::new)
                .toList();
    }

    public List<AppointmentDetailResponse> listToday(Long userId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay(); // 00:00
        LocalDateTime end = LocalDateTime.now().toLocalDate().atTime(23, 59, 59); // 23:59

        return listByInterval(userId, start, end);
    }

    public List<AppointmentDetailResponse> listUpcomingWeek(Long userId) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7).withHour(23).withMinute(59);

        return listByInterval(userId, start, end);
    }

    public List<AppointmentDetailResponse> listCurrentMonth(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .toLocalDate().atTime(23, 59, 59);

        return listByInterval(userId, start, end);
    }

    private void validateBusinessHours(LocalDateTime start, LocalDateTime end) {

        if (start.getDayOfWeek() == DayOfWeek.SUNDAY || start.getDayOfWeek() == DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("O estabelecimento não abre às segundas-feiras e domingos.");
        }

        LocalTime timeStart = start.toLocalTime();
        LocalTime timeEnd = end.toLocalTime();

        if (timeStart.isBefore(OPENING_TIME)) {
            throw new IllegalArgumentException("O estabelecimento abre às " + OPENING_TIME);
        }

        if (timeEnd.isAfter(CLOSING_TIME.plusMinutes(20))) {
            throw new IllegalArgumentException("O estabelecimento fecha às " + CLOSING_TIME + ". O serviço deve terminar em até 20 minutos depois do fechamento!.");
        }
    }
}
