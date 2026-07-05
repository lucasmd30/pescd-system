package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.GradeOption;
import br.ufscar.pescd.enums.StudentOfferStatus;

import java.time.LocalDateTime;

public record OfferStudentSummaryResponse(
        Long enrollmentId,
        UserSummaryResponse student,
        UserSummaryResponse supervisor,
        StudentOfferStatus status,
        String statusLabel,
        LocalDateTime enrolledAt,
        Boolean hasWorkPlan,
        Boolean hasDocumentation,
        Boolean hasReport,
        String completionSource,
        Integer finalFrequency,
        GradeOption finalGrade,
        String finalGradeLabel
) {
}
