package br.ufscar.pescd.dto;

public record SecretaryCloseOfferPreviewResponse(
        OfferSummaryResponse offer,
        boolean canClose,
        String instructions
) {
}
