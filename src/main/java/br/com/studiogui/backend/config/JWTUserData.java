package br.com.studiogui.backend.config;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Builder
public record JWTUserData(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
} 
