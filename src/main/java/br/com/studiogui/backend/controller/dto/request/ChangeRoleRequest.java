package br.com.studiogui.backend.controller.dto.request;

import br.com.studiogui.backend.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "O novo cargo é obrigatório")
        UserRole role
) {
}
