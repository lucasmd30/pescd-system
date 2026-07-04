package br.ufscar.pescd.dto;

public record PublicUserResponse(
        Long id,
        String fullName,
        String email,
        String username,
        String role,
        boolean enabled
) {
}
