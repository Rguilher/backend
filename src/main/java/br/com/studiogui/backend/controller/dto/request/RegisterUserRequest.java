package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record RegisterUserRequest(
    @NotEmpty(message = "Name is requied.") String name,
    @NotEmpty(message = "Please provide an email address.") String email,
    @NotEmpty(message = "Password is requied.") String password,
    @NotEmpty(message = "Please provide a phone number.") String phone) {
}
