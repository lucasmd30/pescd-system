package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.StudentOfferStatus;

import java.time.LocalDateTime;

public record StatusLogResponse(
        Long id,
        StudentOfferStatus previousStatus,
        String previousStatusLabel,
        StudentOfferStatus newStatus,
        String newStatusLabel,
        String description,
        LocalDateTime changedAt
) {
}
