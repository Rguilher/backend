package br.com.studiogui.backend.model;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"),
    PROFESSIONAL("professional"),
    USER("user");

    private final String role;

    UserRole(String role){
        this.role = role;
    }

}
