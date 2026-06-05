package br.ufscar.pescd.enums;

public enum StudentOfferStatus {
    NAO_ENVIADO,
    PLANO_ENVIADO,
    PLANO_APROVADO,
    DOCUMENTACAO_ENVIADA,
    RELATORIO_ENVIADO,
    RELATORIO_APROVADO_SUPERVISOR,
    CONCLUIDO_RESPONSAVEL;

    public String getDisplayName() {
        return switch (this) {
            case NAO_ENVIADO -> "Não enviado";
            case PLANO_ENVIADO -> "Plano enviado";
            case PLANO_APROVADO -> "Plano aprovado";
            case DOCUMENTACAO_ENVIADA -> "Documentação enviada";
            case RELATORIO_ENVIADO -> "Relatório enviado";
            case RELATORIO_APROVADO_SUPERVISOR -> "Relatório aprovado pelo supervisor";
            case CONCLUIDO_RESPONSAVEL -> "Concluído pelo responsável";
        };
    }
}
