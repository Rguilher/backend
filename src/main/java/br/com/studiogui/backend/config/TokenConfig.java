package br.com.studiogui.backend.config;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.studiogui.backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.studiogui.backend.model.User;

@Component
public class TokenConfig {

    // WARNING: Hardcoded secret for simulation purposes only.
    // In production, fetch this from environment variables or a vault.
    // Using for tests.
    private String secret = "secret";
    private final UserRepository userRepository;
    Algorithm algorithm = Algorithm.HMAC256(secret);

    public TokenConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(User user, Long expiresIn) {
	    List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withClaim("userId", user.getId())
                .withClaim("roles", roles)
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(expiresIn))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

    public Optional<JWTUserData> validateToken(String accessToken) {

        try {
            DecodedJWT decode = JWT.require(algorithm).build().verify(accessToken);
            List<String> roles = decode.getClaim("roles").asList(String.class);

            return Optional.of(
                    JWTUserData.builder()
                            .userId(decode.getClaim("userId").asLong())
                            .username(decode.getSubject())
                            .authorities(
                                    roles != null ? roles.stream().map(SimpleGrantedAuthority::new).toList() : List.of()
                            )
                            .build());
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }

    }

}
