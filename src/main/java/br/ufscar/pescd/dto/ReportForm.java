package br.ufscar.pescd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReportForm {

    @NotNull(message = "O indicador de frequência é obrigatório.")
    @Min(value = 0, message = "A frequência deve ser entre 0 e 100.")
    @Max(value = 100, message = "A frequência deve ser entre 0 e 100.")
    private Integer frequency;

    private MultipartFile file;
}
