package br.ufscar.pescd.enums;

public enum UserRole {
    ADMIN,
    SECRETARIO,
    ALUNO,
    PROFESSOR;

    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "Administrador";
            case SECRETARIO -> "Secretário";
            case ALUNO -> "Aluno";
            case PROFESSOR -> "Professor";
        };
    }
}
