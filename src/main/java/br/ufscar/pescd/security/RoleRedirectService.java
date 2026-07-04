package br.ufscar.pescd.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleRedirectService {

    public String resolveRedirectPath(Collection<? extends GrantedAuthority> authorities) {
        Set<String> authoritySet = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (authoritySet.contains("ROLE_ADMIN")) {
            return "/admin/users";
        }

        if (authoritySet.contains("ROLE_SECRETARIO")) {
            return "/secretary/offers";
        }

        if (authoritySet.contains("ROLE_PROFESSOR")) {
            return "/dashboard/professor";
        }

        if (authoritySet.contains("ROLE_ALUNO")) {
            return "/aluno/ofertas";
        }

        return "/dashboard";
    }
}
