package br.ufscar.pescd.dto;

import java.time.LocalDate;

public record OfferView(
        String name,
        String semester,
        LocalDate startDate,
        LocalDate endDate,
        String responsibleProfessor,
        long enrolledStudents
) {
}
