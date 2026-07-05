package br.ufscar.pescd.dto;

/**
 * AL.01 - resumo de uma oferta na qual o aluno está inscrito, com o status da
 * sua participação. Combina os dados da oferta com o resumo da inscrição.
 */
public record StudentOfferSummaryResponse(
        OfferSummaryResponse offer,
        OfferStudentSummaryResponse enrollment
) {
}
