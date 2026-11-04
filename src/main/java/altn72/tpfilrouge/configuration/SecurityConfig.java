package altn72.tpfilrouge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String PAGE_CONNEXION = "/connexion";
    private static final String URL_DECONNEXION = "/deconnexion";

    @Bean
    @Order(1)
    public SecurityFilterChain filtreApi(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(autorisations -> autorisations.anyRequest().authenticated())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint((requete, reponse, exception) ->
                                repondreErreurApi(reponse, 401, "Connexion requise"))
                        .accessDeniedHandler((requete, reponse, exception) ->
                                repondreErreurApi(reponse, 403, "Profil non autorisé ou jeton CSRF invalide")));
        return http.build();
    }

    private void repondreErreurApi(HttpServletResponse reponse, int statut, String detail) throws IOException {
        reponse.setStatus(statut);
        reponse.setCharacterEncoding("UTF-8");
        reponse.setContentType("application/problem+json");
        reponse.getWriter().write("{\"type\":\"about:blank\",\"status\":" + statut
                + ",\"title\":\"Accès refusé\",\"detail\":\"" + detail + "\"}");
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filtreSecurite(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers(PAGE_CONNEXION, "/css/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formulaire -> formulaire
                        .loginPage(PAGE_CONNEXION)
                        .loginProcessingUrl(PAGE_CONNEXION)
                        .usernameParameter("login")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/accueil", true) //mettre le chemin du tableau de bord plus tard
                        .failureUrl(PAGE_CONNEXION + "?erreur"))
                .logout(deconnexion -> deconnexion
                        .logoutUrl(URL_DECONNEXION)
                        .logoutSuccessUrl(PAGE_CONNEXION + "?deconnexion")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder();
    }
}
