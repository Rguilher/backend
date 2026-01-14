package br.com.studiogui.backend.service;

import br.com.studiogui.backend.controller.dto.request.CreateServiceRequest;
import br.com.studiogui.backend.model.SalonService;
import br.com.studiogui.backend.repository.SalonServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private SalonServiceService serviceService;

    @Mock
    private SalonServiceRepository repository;

    @Test
    @DisplayName("Deve criar serviço se nome for único")
    void shouldCreateService() {
        CreateServiceRequest request = new CreateServiceRequest("Corte", "Desc", BigDecimal.TEN, 30);
        when(repository.existsByNameAndActiveTrue("Corte")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SalonService created = serviceService.createService(request);

        assertTrue(created.getActive());
        assertEquals("Corte", created.getName());
    }

    @Test
    @DisplayName("Deve falhar ao criar serviço com nome duplicado")
    void shouldFailCreationIfNameExists() {
        CreateServiceRequest request = new CreateServiceRequest("Corte", "Desc", BigDecimal.TEN, 30);
        when(repository.existsByNameAndActiveTrue("Corte")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> serviceService.createService(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Delete deve apenas inativar o serviço (Soft Delete)")
    void shouldSoftDeleteService() {
        Long id = 1L;
        SalonService service = new SalonService();
        service.setId(id);
        service.setActive(true);

        when(repository.findById(id)).thenReturn(Optional.of(service));

        serviceService.deleteService(id);

        assertFalse(service.getActive()); // Verifica se virou false
        verify(repository).save(service); // Verifica se salvou a alteração
    }
}
