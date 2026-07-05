package br.ufscar.pescd.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SecretaryCloseOfferRequest(
        @NotNull(message = "A confirmação de encerramento é obrigatória.")
        @AssertTrue(message = "A confirmação de encerramento é obrigatória.")
        Boolean confirm
) {
}
