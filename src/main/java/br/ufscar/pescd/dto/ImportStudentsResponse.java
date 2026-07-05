package br.ufscar.pescd.dto;

/**
 * S.02 - resultado da importação de alunos via CSV: quantidade de alunos
 * efetivamente inscritos e o estado atualizado da oferta.
 */
public record ImportStudentsResponse(
        int enrolled,
        OfferDetailsResponse offer
) {
}
