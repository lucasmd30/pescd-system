package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.GradeOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConcludeReportFormDTO {

    @NotBlank(message = "O parecer é obrigatório.")
    private String parecer;

    @NotNull(message = "A frequência é obrigatória.")
    @Min(value = 0, message = "A frequência deve ser entre 0 e 100.")
    @Max(value = 100, message = "A frequência deve ser entre 0 e 100.")
    private Integer frequencia;

    @NotNull(message = "A nota é obrigatória.")
    private GradeOption nota;
}
