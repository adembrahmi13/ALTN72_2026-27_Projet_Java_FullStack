package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Contexte distinct : ne pas réutiliser le repository CSRF de test installé par .with(csrf()).
// Ce test valide les vrais cookies et en-têtes utilisés par Swagger, sans jeton simulé.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:swagger;MODE=MariaDB;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@Transactional
class SwaggerSessionTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Value("${demo.mot-de-passe}") String motDePasse;

    @Test
    void sessionReelleEtCookieCsrfPermettentResolutionCommeSwagger() throws Exception {
        Station station = stations.save(new Station("Station Swagger"));
        Equipement equipement = equipements.save(new Equipement("EQ-SWAGGER", "Borne", "Borne", "En panne", station));
        Incident incident = incidents.save(new Incident(equipement, "Panne", LocalDateTime.now().minusHours(2),
                Incident.STATUT_NON_AFFECTE, null, null));
        var connexion = mvc.perform(get("/connexion")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(connexion);
        var login = mvc.perform(post("/connexion").cookie(connexion)
                        .header("X-XSRF-TOKEN", connexion.getValue())
                        .param("login", "responsable").param("motDePasse", motDePasse))
                .andExpect(redirectedUrl("/accueil")).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        var cookie = mvc.perform(get("/v3/api-docs").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).session(session)
                        .cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.incident.statut").value("Résolu"));
    }
}
