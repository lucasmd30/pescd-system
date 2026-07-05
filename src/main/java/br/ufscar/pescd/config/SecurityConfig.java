package br.ufscar.pescd.config;

import br.ufscar.pescd.security.CustomUserDetailsService;
import br.ufscar.pescd.security.RoleBasedAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final RoleBasedAuthenticationSuccessHandler successHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            RoleBasedAuthenticationSuccessHandler successHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/offers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/secretary/**").hasRole("SECRETARIO")
                        .requestMatchers("/api/professor/responsavel/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/professor/supervisor/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/secretary/**").hasRole("SECRETARIO")
                        .requestMatchers("/aluno/**").hasRole("ALUNO")
                        .requestMatchers("/professor/supervisor/**").hasRole("PROFESSOR")
                        .requestMatchers("/professor/responsavel/**").hasRole("PROFESSOR")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> writeJsonError(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized",
                                        "Autenticação necessária para acessar este recurso."
                                ),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                        .defaultAccessDeniedHandlerFor(
                                (request, response, accessDeniedException) -> writeJsonError(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden",
                                        "Você não tem permissão para acessar este recurso."
                                ),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                        .accessDeniedPage("/access-denied")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    private void writeJsonError(
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format(
                        "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                        status,
                        escapeJson(error),
                        escapeJson(message)
                )
        );
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
