package br.ufscar.pescd.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ResponsibleCloseOfferSummaryResponse(
        OfferSummaryResponse offer,
        List<OfferStudentSummaryResponse> students,
        BigDecimal averageFrequency,
        long documentationCompletions,
        long reportCompletions,
        Map<String, Long> gradeDistribution,
        boolean canClose
) {
}
