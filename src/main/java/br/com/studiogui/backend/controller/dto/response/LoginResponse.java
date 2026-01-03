package br.com.studiogui.backend.controller.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(String accessToken, Long expiresIn) {

}
