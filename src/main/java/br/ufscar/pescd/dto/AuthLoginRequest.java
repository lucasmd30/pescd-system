package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "O nome de usuário é obrigatório.")
        String username,
        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}
