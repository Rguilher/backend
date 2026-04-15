package br.com.studiogui.backend.controller;

import br.com.studiogui.backend.config.JWTUserData;
import br.com.studiogui.backend.controller.dto.request.ChangeRoleRequest;
import br.com.studiogui.backend.controller.dto.request.UpdatePasswordRequest;
import br.com.studiogui.backend.controller.dto.response.UserResponse;
import br.com.studiogui.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Altera o cargo de um usuário.
     * Restrito apenas a administradores.
     * @param id -> ID do usuário que terá o cargo alterado (Alvo)
     * @param request ->  DTO contendo o novo UserRole
     * @param currentUser -> Dados do usuário autenticado extraídos do Token JWT
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@PathVariable Long id,
            @RequestBody @Valid ChangeRoleRequest request,
            @AuthenticationPrincipal JWTUserData currentUser) {
        userService.changeUserRole(id, request, currentUser.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findUser(@RequestParam("email") String email) {
        var user = userService.findUser(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profissional")
    public ResponseEntity<Page<UserResponse>> findProfessional(
            @PageableDefault(size = 10, sort = "name") Pageable pageable){
        var page = userService.findProfessional(pageable);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @RequestBody @Valid UpdatePasswordRequest request,
            @AuthenticationPrincipal JWTUserData currentUser) {

        userService.updatePassword(currentUser.userId(), request);
        return ResponseEntity.noContent().build();
    }

}
