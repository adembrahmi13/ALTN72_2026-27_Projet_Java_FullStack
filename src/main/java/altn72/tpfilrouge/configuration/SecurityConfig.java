package altn72.tpfilrouge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String PAGE_CONNEXION = "/connexion";
    private static final String URL_DECONNEXION = "/deconnexion";

    @Bean
    public SecurityFilterChain filtreSecurite(HttpSecurity http) throws Exception {
        http
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