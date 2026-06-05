package br.ufscar.pescd.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (authorities.contains("ROLE_ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        if (authorities.contains("ROLE_SECRETARIO")) {
            response.sendRedirect(request.getContextPath() + "/secretary/offers");
            return;
        }

        if (authorities.contains("ROLE_PROFESSOR")) {
            response.sendRedirect(request.getContextPath() + "/dashboard/professor");
            return;
        }

        if (authorities.contains("ROLE_ALUNO")) {
            response.sendRedirect(request.getContextPath() + "/dashboard/aluno");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
