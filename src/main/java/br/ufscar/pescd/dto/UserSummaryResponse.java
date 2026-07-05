package br.ufscar.pescd.dto;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String username
) {
}
