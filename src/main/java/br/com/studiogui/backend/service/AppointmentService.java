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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository serviceRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, SalonServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
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

        LocalDateTime dayStart = newStart.toLocalDate().atStartOfDay(); // 00:00:00
        LocalDateTime dayEnd = newStart.toLocalDate().atTime(23, 59, 59); // 23:59:59

        List<Appointment> scheduled = appointmentRepository.findProfessionalAgenda(professional, dayStart, dayEnd);
        for (Appointment iScheduled : scheduled) {
            LocalDateTime existingStart = iScheduled.getDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(iScheduled.getService().getDurationMin());

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new IllegalArgumentException("Conflito de horário! O profissional já possui agendamento das "
                        + existingStart.toLocalTime() + " às " + existingEnd.toLocalTime());
            }
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
}
