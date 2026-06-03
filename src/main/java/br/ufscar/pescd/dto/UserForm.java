package br.ufscar.pescd.dto;

import br.ufscar.pescd.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {

    private Long id;

    @NotBlank(message = "O nome completo é obrigatório.")
    private String fullName;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "O nome de usuário é obrigatório.")
    private String username;

    private String password;

    @NotNull(message = "O perfil é obrigatório.")
    private UserRole role;
}
