package br.ufscar.pescd.dto;

import java.util.List;

/**
 * AL.01/AL.04 - detalhes completos da inscrição do aluno em uma oferta: dados da
 * oferta, resumo da inscrição, envios já realizados (plano, documentação e
 * relatório) e o histórico de mudanças de status.
 */
public record StudentEnrollmentDetailsResponse(
        OfferSummaryResponse offer,
        OfferStudentSummaryResponse enrollment,
        WorkPlanDetailsResponse workPlan,
        DocumentationDetailsResponse documentation,
        ReportDetailsResponse report,
        List<StatusLogResponse> statusLogs
) {
}
