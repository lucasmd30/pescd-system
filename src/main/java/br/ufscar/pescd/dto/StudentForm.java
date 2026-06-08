package br.ufscar.pescd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentForm {

    @NotBlank(message = "O nome completo é obrigatório.")
    private String fullName;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "O RA é obrigatório.")
    private String ra;
}
