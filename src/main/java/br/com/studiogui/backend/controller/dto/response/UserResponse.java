package br.com.studiogui.backend.controller.dto.response;

import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.model.enums.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
