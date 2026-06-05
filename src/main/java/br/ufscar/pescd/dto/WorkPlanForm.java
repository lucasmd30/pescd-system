package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class WorkPlanForm {

    @NotBlank(message = "O código da disciplina é obrigatório.")
    private String disciplineCode;

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    private String disciplineName;

    @NotBlank(message = "O curso da disciplina é obrigatório.")
    private String disciplineCourse;

    @NotNull(message = "O professor supervisor é obrigatório.")
    private Long supervisorId;

    private MultipartFile file;
}
