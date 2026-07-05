package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.GradeOption;

import java.time.LocalDateTime;

public record DocumentationDetailsResponse(
        Long id,
        String institutionName,
        String disciplineName,
        String disciplineCourse,
        Integer workloadHours,
        String fileName,
        String contentType,
        LocalDateTime submittedAt,
        String responsavelParecer,
        Integer responsavelFrequencia,
        GradeOption responsavelNota,
        String responsavelNotaLabel,
        LocalDateTime responsavelApprovedAt
) {
}
