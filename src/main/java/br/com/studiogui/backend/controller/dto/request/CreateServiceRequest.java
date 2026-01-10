package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateServiceRequest(
        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,

        @NotBlank(message = "A descrição é obrigatória")
        String description,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "A duração é obrigatória")
        @Min(value = 15, message = "A duração mínima é 15 minutos")
        Integer durationMin
) {
}
