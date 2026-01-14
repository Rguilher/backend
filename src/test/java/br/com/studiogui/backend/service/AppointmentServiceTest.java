package br.com.studiogui.backend.service;
import br.com.studiogui.backend.controller.dto.request.CreateAppointmentRequest;
import br.com.studiogui.backend.model.Appointment;
import br.com.studiogui.backend.model.SalonService;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.model.enums.AppointmentStatus;
import br.com.studiogui.backend.model.enums.UserRole;
import br.com.studiogui.backend.repository.AppointmentRepository;
import br.com.studiogui.backend.repository.SalonServiceRepository;
import br.com.studiogui.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SalonServiceRepository serviceRepository;

    // Utilitário para pegar uma data futura válida (Próxima terça às 14:00)
    // Evita testar com Domingo ou fora do horário comercial
    private LocalDateTime getValidFutureDate() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                .withHour(14).withMinute(0).withSecond(0);
    }

    @Test
    @DisplayName("Deve agendar com sucesso (Caminho Feliz)")
    void shouldScheduleSuccessfully() {
        Long clientId = 1L;
        Long profId = 2L;
        Long serviceId = 3L;
        LocalDateTime date = getValidFutureDate();

        User client = new User(); client.setId(clientId); client.setRole(UserRole.USER);
        User prof = new User(); prof.setId(profId); prof.setRole(UserRole.PROFESSIONAL);
        SalonService service = new SalonService(); service.setId(serviceId); service.setActive(true); service.setDurationMin(60);

        CreateAppointmentRequest request = new CreateAppointmentRequest(profId, serviceId, date);

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(userRepository.findById(profId)).thenReturn(Optional.of(prof));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // MOCK DO NOVO MÉTODO OTIMIZADO (Deve retornar false, pois não há conflito)
        when(appointmentRepository.existsConflictingAppointment(any(), any(), any(), any())).thenReturn(false);

        appointmentService.schedule(request, clientId);

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve falhar se for fora do horário comercial (ex: 20:00)")
    void shouldFailOutsideBusinessHours() {
        LocalDateTime date = getValidFutureDate().withHour(20); // 20:00 (Fechado)
        CreateAppointmentRequest request = new CreateAppointmentRequest(2L, 3L, date);

        mockBasicDependencies(); // Método auxiliar abaixo

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, 1L));

        assertTrue(ex.getMessage().contains("fecha às"));
    }

    @Test
    @DisplayName("Deve falhar se for Domingo")
    void shouldFailOnSunday() {
        LocalDateTime sunday = LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).withHour(10);
        CreateAppointmentRequest request = new CreateAppointmentRequest(2L, 3L, sunday);

        mockBasicDependencies();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, 1L));

        assertTrue(ex.getMessage().contains("domingos"));
    }

    @Test
    @DisplayName("Deve falhar se antecedência for menor que 30 minutos")
    void shouldFailIfTooSoon() {
        // Tenta agendar para "daqui a 5 minutos"
        LocalDateTime nowPlus5 = LocalDateTime.now().plusMinutes(5);
        CreateAppointmentRequest request = new CreateAppointmentRequest(2L, 3L, nowPlus5);

        mockBasicDependencies();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.schedule(request, 1L));

        assertTrue(ex.getMessage().contains("antecedência"));
    }

    @Test
    @DisplayName("Deve falhar se houver conflito de horário (Banco retornou True)")
    void shouldFailIfConflictExists() {
        LocalDateTime date = getValidFutureDate();
        CreateAppointmentRequest request = new CreateAppointmentRequest(2L, 3L, date);

        mockBasicDependencies();

        // SIMULA O CONFLITO VINDO DO BANCO
        when(appointmentRepository.existsConflictingAppointment(any(), any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> appointmentService.schedule(request, 1L));

        verify(appointmentRepository, never()).save(any());
    }

    // --- Auxiliares para evitar repetição de código (Dica Senior: DRY nos testes) ---
    private void mockBasicDependencies() {
        User client = new User(); client.setId(1L);
        User prof = new User(); prof.setId(2L); prof.setRole(UserRole.PROFESSIONAL);
        SalonService service = new SalonService(); service.setActive(true); service.setDurationMin(60);

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(prof));
        lenient().when(serviceRepository.findById(3L)).thenReturn(Optional.of(service));
    }
}
