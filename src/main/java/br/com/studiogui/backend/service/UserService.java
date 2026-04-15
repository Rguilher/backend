package br.com.studiogui.backend.service;

import br.com.studiogui.backend.controller.dto.request.ChangeRoleRequest;
import br.com.studiogui.backend.controller.dto.request.UpdatePasswordRequest;
import br.com.studiogui.backend.controller.dto.response.UserResponse;
import br.com.studiogui.backend.model.enums.UserRole;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.studiogui.backend.controller.dto.request.RegisterUserRequest;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Este e-mail já está em uso.");
        }

        var user = User
                .builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(UserRole.USER)
                .root(false)
                .build();
        
        return userRepository.save(user);
    }

    @Transactional
    public void changeUserRole(Long targetUserId, ChangeRoleRequest data, Long currentUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário alvo não encontrado"));

        if (Boolean.TRUE.equals(targetUser.getRoot())) {
            throw new IllegalArgumentException("Este usuário é um Super Admin (Root) e não pode ser modificado.");
        }
        if (targetUser.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Você não pode alterar o seu próprio cargo.");
        }
        targetUser.setRole(data.role());
        userRepository.save(targetUser);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("A nova senha não pode ser igual à senha atual.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findUser(String email) {
        var user = userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return new UserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findProfessional(Pageable pageable) {
        return userRepository.findByRole(UserRole.PROFESSIONAL ,pageable)
                .map(UserResponse::new);
    }
}
