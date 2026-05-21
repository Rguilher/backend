package br.com.studiogui.backend.service;

import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordRecoveryService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestPasswordRecovery(String email) {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("Tentativa de recuperação de senha para e-mail não cadastrado: {}", email);
            return;
        }

        User user = optionalUser.get();
        String code = generate6DigitCode();

        user.setRecoveryCode(code);
        user.setRecoveryCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendPasswordRecoveryEmail(email, code);
        log.info("Processo de recuperação de senha iniciado para o usuário ID: {}", user.getId());
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Dados inválidos."));

        if (user.getRecoveryCode() == null || !user.getRecoveryCode().equals(code)) {
            log.warn("Tentativa falha de redefinição de senha com código inválido para o usuário ID: {}", user.getId());
            throw new IllegalArgumentException("Código inválido ou não solicitado.");
        }

        if (user.getRecoveryCodeExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Tentativa de uso de código de recuperação expirado para o usuário ID: {}", user.getId());
            throw new IllegalArgumentException("Código expirado. Solicite um novo.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRecoveryCode(null);
        user.setRecoveryCodeExpiry(null);
        userRepository.save(user);

        log.info("Senha redefinida com sucesso para o usuário ID: {}", user.getId());
    }

    private String generate6DigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}