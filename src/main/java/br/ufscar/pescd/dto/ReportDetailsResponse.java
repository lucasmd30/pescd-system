package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.GradeOption;

import java.time.LocalDateTime;

public record ReportDetailsResponse(
        Long id,
        Integer frequency,
        String fileName,
        String contentType,
        LocalDateTime submittedAt,
        String supervisorParecer,
        Integer supervisorFrequencia,
        GradeOption supervisorNotaSugestao,
        String supervisorNotaSugestaoLabel,
        LocalDateTime supervisorApprovedAt,
        String responsavelParecer,
        Integer responsavelFrequencia,
        GradeOption responsavelNota,
        String responsavelNotaLabel,
        LocalDateTime responsavelApprovedAt
) {
}
