package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String code,
        @NotBlank @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
        String newPassword
) {
}
