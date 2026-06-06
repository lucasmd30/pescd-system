package br.ufscar.pescd.enums;

public enum GradeOption {

    A("A — Excelente"),
    B("B — Bom"),
    C("C — Regular"),
    D("D — Insuficiente"),
    E("E — Reprovado");

    private final String displayName;

    GradeOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
