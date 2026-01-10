package br.com.studiogui.backend.controller.dto.response;

import lombok.Builder;

@Builder
public record RegisterUserResponse(
        String name, String email, String phone) {
}
