package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.OfferStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OfferSummaryResponse(
        Long id,
        String name,
        String semester,
        LocalDate startDate,
        LocalDate endDate,
        OfferStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime closureRequestedAt,
        LocalDateTime closedAt,
        String lessonsLearned,
        UserSummaryResponse responsibleProfessor,
        UserSummaryResponse closureRequestedBy,
        UserSummaryResponse closedBy,
        long enrolledStudents
) {
}
