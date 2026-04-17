package br.com.studiogui.backend.service;

import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordRecoveryService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordRecoveryService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestPasswordRecovery(String email) {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        String code = generate6DigitCode();

        user.setRecoveryCode(code);
        user.setRecoveryCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // TODO: Para o MVP, simular envio. No futuro, plugar AWS SES, SendGrid ou JavaMailSender.
        System.out.println("=======================================");
        System.out.println("E-MAIL ENVIADO PARA: " + email);
        System.out.println("CÓDIGO DE RECUPERAÇÃO: " + code);
        System.out.println("=======================================");
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Dados inválidos."));

        if (user.getRecoveryCode() == null || !user.getRecoveryCode().equals(code)) {
            throw new IllegalArgumentException("Código inválido ou não solicitado.");
        }

        if (user.getRecoveryCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado. Solicite um novo.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRecoveryCode(null);
        user.setRecoveryCodeExpiry(null);
        userRepository.save(user);
    }

    private String generate6DigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
