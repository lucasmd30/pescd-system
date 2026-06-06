package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApprovePlanFormDTO {

    @NotBlank(message = "O parecer é obrigatório.")
    private String parecer;
}
