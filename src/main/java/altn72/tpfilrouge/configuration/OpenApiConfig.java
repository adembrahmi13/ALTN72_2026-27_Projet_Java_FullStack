package altn72.tpfilrouge.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI documentationApi() {
        return new OpenAPI().info(new Info().title("Guardian — API des incidents").version("1.0")
                .description("""
                        Connectez-vous d'abord via /connexion, puis ouvrez Swagger dans le même navigateur.
                        Les deux profils peuvent consulter. Seul le responsable peut résoudre un incident.
                        La session et la protection CSRF sont conservées ; Swagger utilise les cookies existants.
                        Les erreurs sont renvoyées en application/problem+json.
                        """))
                .components(new Components().addSecuritySchemes("session", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID")
                        .description("Session créée par le formulaire /connexion ; réutilisée par le navigateur.")));
    }
}
