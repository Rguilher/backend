package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
        @NotEmpty(message = "Please provide an email address.") String username,
        @NotEmpty(message = "Please provide a password.") String password) {
}
