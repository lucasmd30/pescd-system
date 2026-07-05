package br.ufscar.pescd.dto;

import java.util.List;

public record AuthResponse(
        Long id,
        String fullName,
        String email,
        String username,
        String role,
        List<String> authorities,
        String redirectPath
) {
}
