package br.com.studiogui.backend.config;

import java.time.Instant;
import java.util.Optional;

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

    Algorithm algorithm = Algorithm.HMAC256(secret);

    public String generateToken(User user, Long expiresIn) {
        return JWT.create()
                .withClaim("userId", user.getId())
                .withSubject(user.getEmail())
                .withExpiresAt(Instant.now().plusSeconds(expiresIn))
                .withIssuedAt(Instant.now())
                .sign(algorithm);
    }

    public Optional<JWTUserData> validateToken(String accessToken) {

        try {
            DecodedJWT decode = JWT.require(algorithm).build().verify(accessToken);

            return Optional.of(
                    JWTUserData.builder()
                            .userId(decode.getClaim("userId").asLong())
                            .username(decode.getSubject()).build());
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }

    }

}
