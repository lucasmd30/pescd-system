package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentationForm {

    @NotBlank(message = "O nome da instituição é obrigatório.")
    private String institutionName;

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    private String disciplineName;

    @NotBlank(message = "O curso da disciplina é obrigatório.")
    private String disciplineCourse;

    @NotNull(message = "A carga horária é obrigatória.")
    @Positive(message = "A carga horária deve ser maior que zero.")
    private Integer workloadHours;

    private MultipartFile file;
}
