package br.ufscar.pescd.dto;

import java.time.LocalDateTime;

public record WorkPlanDetailsResponse(
        Long id,
        String disciplineCode,
        String disciplineName,
        String disciplineCourse,
        String fileName,
        String contentType,
        LocalDateTime submittedAt,
        String supervisorParecer,
        LocalDateTime supervisorApprovedAt
) {
}
