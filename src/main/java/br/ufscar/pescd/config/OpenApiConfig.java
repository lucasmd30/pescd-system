package br.ufscar.pescd.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PESCD REST API",
                version = "v1",
                description = "Documentação completa das rotas REST do sistema PESCD.",
                contact = @Contact(name = "Equipe PESCD")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente local")
        },
        security = @SecurityRequirement(name = "sessionAuth")
)
@SecurityScheme(
        name = "sessionAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID",
        description = "Autenticação baseada na sessão HTTP criada após o login em /api/auth/login."
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi restApi() {
        return GroupedOpenApi.builder()
                .group("pescd-rest-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
