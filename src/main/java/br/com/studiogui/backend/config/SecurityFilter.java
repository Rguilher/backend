package br.com.studiogui.backend.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenConfig tokenConfig;

    public SecurityFilter(TokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String authorizedHeader = request.getHeader("Authorization");
        if (authorizedHeader != null && authorizedHeader.startsWith("Bearer ")) {

            String accessToken = authorizedHeader.substring("Bearer ".length());
            Optional<JWTUserData> optUser = tokenConfig.validateToken(accessToken);
            if (optUser.isPresent()) {
                JWTUserData userData = optUser.get();
                var authentication = new UsernamePasswordAuthenticationToken(
                        userData, null, userData.authorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
