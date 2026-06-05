package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class OfferForm {

    private Long id;

    private String name;

    @NotBlank(message = "O semestre é obrigatório.")
    private String semester;

    @NotNull(message = "A data de início é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "A data de fim é obrigatória.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NotNull(message = "O professor responsável é obrigatório.")
    private Long responsibleProfessorId;
}
