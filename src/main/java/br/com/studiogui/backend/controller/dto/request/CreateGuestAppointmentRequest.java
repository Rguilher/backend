package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateGuestAppointmentRequest(
        @NotBlank(message = "O nome do cliente é obrigatório para agendamentos avulsos")
        String guestName,

        @NotBlank(message = "O telefone do cliente é obrigatório")
        String guestPhone,

        @NotNull(message = "Selecione o profissional")
        Long professionalId,

        @NotNull(message = "Selecione o serviço")
        Long serviceId,

        @NotNull(message = "A data e hora são obrigatórias")
        @Future(message = "O agendamento deve ser para o futuro")
        LocalDateTime startTime
) {
}
