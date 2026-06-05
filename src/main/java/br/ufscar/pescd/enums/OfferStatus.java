package br.ufscar.pescd.enums;

public enum OfferStatus {
    EM_ANDAMENTO,
    AGUARDANDO_ENCERRAMENTO_SECRETARIO,
    CONCLUIDA,
    EM_ATRASO;

    public String getDisplayName() {
        return switch (this) {
            case EM_ANDAMENTO -> "Em andamento";
            case AGUARDANDO_ENCERRAMENTO_SECRETARIO -> "Aguardando encerramento do secretário";
            case CONCLUIDA -> "Concluída";
            case EM_ATRASO -> "Em atraso";
        };
    }
}
