package br.com.studiogui.backend.config;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.studiogui.backend.controller.dto.request.LoginRequest;
import br.com.studiogui.backend.controller.dto.response.LoginResponse;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.repository.UserRepository;

@Service
public class AuthConfigService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthConfigService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

}
