package br.ufscar.pescd.dto;

import java.util.List;

public record OfferStudentDetailsResponse(
        OfferSummaryResponse offer,
        OfferStudentSummaryResponse enrollment,
        List<StatusLogResponse> statusLogs
) {
}
