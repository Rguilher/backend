package br.com.studiogui.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){

        return http
                .csrf(csrf -> csrf.disable())
                // TODO: Enable and configure CORS properly before production
                //.cors(cors -> cors.configure(http))
                .sessionManagement(session -> session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                ))
                .authorizeHttpRequests(authorize -> authorize
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
