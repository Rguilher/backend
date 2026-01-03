package br.com.studiogui.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.studiogui.backend.config.AuthConfigService;
import br.com.studiogui.backend.config.TokenConfig;
import br.com.studiogui.backend.controller.dto.request.LoginRequest;
import br.com.studiogui.backend.controller.dto.request.RegisterUserRequest;
import br.com.studiogui.backend.controller.dto.response.LoginResponse;
import br.com.studiogui.backend.controller.dto.response.RegisterUserResponse;
import br.com.studiogui.backend.model.User;
import br.com.studiogui.backend.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthConfigService authConfigService;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthController(UserService userService, AuthConfigService authConfigService, TokenConfig tokenConfig,
        AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.authConfigService = authConfigService;
        this.tokenConfig = tokenConfig;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        

        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication auth = authenticationManager.authenticate(userAndPass);
        User user = (User) auth.getPrincipal();
        Long expiresIn = 300L;
        String accessToken = tokenConfig.generateToken(user, expiresIn);
        return ResponseEntity.ok(new LoginResponse(accessToken, expiresIn));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {
                
            var user = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            new RegisterUserResponse(user.getName(), user.getEmail(), user.getPhone())
        );
    }
}
