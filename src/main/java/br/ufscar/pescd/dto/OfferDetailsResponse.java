package br.ufscar.pescd.dto;

import java.util.List;

public record OfferDetailsResponse(
        OfferSummaryResponse offer,
        List<OfferStudentSummaryResponse> students
) {
}
