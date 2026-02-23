package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato inválido")
        String email
) {
}
