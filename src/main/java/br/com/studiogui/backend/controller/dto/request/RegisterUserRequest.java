package br.com.studiogui.backend.controller.dto.request;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?9?\\d{4}-?\\d{4}$",
                message = "Telefone inválido. Use o formato (XX) 9XXXX-XXXX")
        String phone
) {
}
