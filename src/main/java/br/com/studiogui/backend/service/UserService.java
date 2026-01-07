package br.com.studiogui.backend.service;

import br.com.studiogui.backend.model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.studiogui.backend.controller.dto.request.RegisterUserRequest;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterUserRequest request) {

        var user = User
                .builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(UserRole.USER)
                .build();
        
        return userRepository.save(user);
    }
}
