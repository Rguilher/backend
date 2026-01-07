package br.com.studiogui.backend.controller.dto.request;

import br.com.studiogui.backend.model.UserRole;
import jakarta.validation.constraints.NotEmpty;

public record RegisterUserRequest(
        @NotEmpty(message = "Name is required.") String name,
        @NotEmpty(message = "Please provide an email address.") String email,
        @NotEmpty(message = "Password is required.") String password,
        @NotEmpty(message = "Please provide a phone number.") String phone
        ) {
}
