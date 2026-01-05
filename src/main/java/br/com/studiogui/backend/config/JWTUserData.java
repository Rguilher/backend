package br.com.studiogui.backend.config;

import lombok.Builder;

@Builder
public record JWTUserData(Long userId, String username) {
} 
