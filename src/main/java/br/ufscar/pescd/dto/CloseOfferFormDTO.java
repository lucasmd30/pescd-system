package br.ufscar.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseOfferFormDTO {

    @NotBlank
    private String lessonsLearned;
}