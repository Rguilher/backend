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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SalonServiceRepository serviceRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User client;
    private User professional;
    private SalonService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentService, "minLeadTimeMinutes", 30);
        ReflectionTestUtils.setField(appointmentService, "openingTime", LocalTime.of(8, 0));
        ReflectionTestUtils.setField(appointmentService, "closingTime", LocalTime.of(18, 0));
        ReflectionTestUtils.setField(appointmentService, "slotDurationMinutes", 45);

        client = User.builder().id(1L).name("Cliente Teste").email("cliente@test.com").role(UserRole.USER).build();
        professional = User.builder().id(2L).name("Profissional Teste").email("pro@test.com").role(UserRole.PROFESSIONAL).build();

        service = new SalonService(1L, "Corte", "Corte Masculino", new BigDecimal("50.00"), 45, true);
    }

    private LocalDateTime getValidFutureTime() {
        LocalDateTime time = LocalDateTime.now().plusDays(5);
        while (time.getDayOfWeek() == DayOfWeek.SUNDAY || time.getDayOfWeek() == DayOfWeek.MONDAY) {
            time = time.plusDays(1);
        }
        return time.withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    @DisplayName("Deve agendar com sucesso quando todos os dados são válidos")
    void shouldScheduleSuccessfully() {
        LocalDateTime validTime = getValidFutureTime();

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                professional.getId(), service.getId(), validTime, client.getId(), null);

        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        when(appointmentRepository.findByClient_IdAndDateTimeBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.existsConflictingAppointment(any(), any(), any(), any())).thenReturn(false);

        AppointmentDetailResponse response = appointmentService.schedule(request, client.getId());

        assertNotNull(response);
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve lançar erro se o cliente tentar agendar consigo mesmo")
    void shouldThrowExceptionWhenClientIsProfessional() {
        LocalDateTime validTime = getValidFutureTime();

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                client.getId(), service.getId(), validTime, client.getId(), null);

        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, client.getId()));

        assertEquals("Você não pode agendar um serviço com você mesmo.", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro se o tempo de antecedência for menor que o limite")
    void shouldThrowExceptionWhenLeadTimeIsTooShort() {
        LocalDateTime invalidTime = LocalDateTime.now().plusMinutes(10);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                professional.getId(), service.getId(), invalidTime, client.getId(), null);

        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, client.getId()));

        assertTrue(exception.getMessage().contains("O agendamento deve ser feito com no mínimo"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro se houver conflito na agenda do profissional")
    void shouldThrowExceptionWhenConflictExists() {
        LocalDateTime validTime = getValidFutureTime();
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                professional.getId(), service.getId(), validTime, client.getId(), null);

        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(appointmentRepository.findByClient_IdAndDateTimeBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        when(appointmentRepository.existsConflictingAppointment(any(), any(), any(), any())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, client.getId()));

        assertEquals("Conflito de horário! O profissional ou você já possuem agendamento neste intervalo.", exception.getMessage());
    }

    @Test
    @DisplayName("O Cliente pode cancelar o próprio agendamento com sucesso")
    void shouldCancelAppointmentSuccessfullyByClient() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setClient(client);
        appointment.setDateTime(LocalDateTime.now().plusDays(2));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        appointmentService.cancelAppointment(10L, client.getId());

        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Deve impedir que um terceiro não-admin cancele o agendamento")
    void shouldThrowExceptionWhenUnauthorizedUserCancels() {
        User hacker = User.builder().id(99L).role(UserRole.USER).build();

        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setClient(client); // Pertence ao cliente, não ao hacker

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(hacker.getId())).thenReturn(Optional.of(hacker));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelAppointment(10L, hacker.getId()));

        assertEquals("Apenas o Cliente ou a Administração podem cancelar este agendamento.", exception.getMessage());
    }

    @Test
    @DisplayName("Disponibilidade deve retornar vazio para datas no passado")
    void shouldReturnEmptyAvailabilityForPastDates() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        List<LocalTime> availability = appointmentService.getAvailability(professional.getId(), pastDate);
        assertTrue(availability.isEmpty());
        verify(appointmentRepository, never()).findByProfessionalAndDate(any(), any(), any());
    }
}