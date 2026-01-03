package br.com.studiogui.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.studiogui.backend.controller.dto.request.LoginRequest;
import br.com.studiogui.backend.controller.dto.request.RegisterUserRequest;
import br.com.studiogui.backend.controller.dto.response.LoginResponse;
import br.com.studiogui.backend.controller.dto.response.RegisterUserResponse;
import br.com.studiogui.backend.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("test")
    public String test(){
        return "test";
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return null;
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
