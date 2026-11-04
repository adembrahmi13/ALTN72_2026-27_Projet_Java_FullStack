package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.EvaluationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
class IncidentApiTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired EvaluationRepository evaluations;
    @Autowired EntityManager em;
    private Incident incident;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station API"));
        Equipement equipement = equipements.save(new Equipement("EQ-API", "Borne API", "Borne", "En panne", station));
        incident = incidents.save(new Incident(equipement, "Écran bloqué", LocalDateTime.now().minusHours(3).withNano(0),
                Incident.STATUT_NON_AFFECTE, null, null));
    }

    @Test
    @WithUserDetails("agent")
    void agentListeIncidentsOuvertsEtResolusDansOrdre() throws Exception {
        Incident ancien = incidents.save(new Incident(incident.getEquipement(), "Ancien", incident.getDateHeureOuverture().minusDays(1),
                Incident.STATUT_RESOLU, null, incident.getDateHeureOuverture().minusHours(1)));
        mvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(incident.getId()))
                .andExpect(jsonPath("$[1].id").value(ancien.getId()))
                .andExpect(jsonPath("$[0].equipement.station.nom").value("Station API"));
    }

    @Test
    void listeVideRetourneTableauJson() throws Exception {
        incidents.delete(incident);
        mvc.perform(get("/api/incidents")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithUserDetails("agent")
    void detailExposeDureeEtRelationsSansEntitesRecursives() throws Exception {
        Prestataire prestataire = new Prestataire("Maintenance API", "contact@example.test");
        em.persist(prestataire);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut(Incident.STATUT_RESOLU);
        incident.setDateHeureResolution(incident.getDateHeureOuverture().plusHours(2));
        evaluations.save(new Evaluation(incident, 5, "Rapide", LocalDate.now()));
        em.flush();
        em.clear();
        mvc.perform(get("/api/incidents/{id}", incident.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident.id").value(incident.getId()))
                .andExpect(jsonPath("$.incident.prestataire.nom").value("Maintenance API"))
                .andExpect(jsonPath("$.incident.equipement.reference").value("EQ-API"))
                .andExpect(jsonPath("$.dureeOuverture").value("0 j 2 h 0 min"))
                .andExpect(jsonPath("$.evaluation.note").value(5))
                .andExpect(jsonPath("$.evaluation.incident").doesNotExist());
    }

    @Test
    void responsableResoutEtReponseJsonRefleteEtatEnregistre() throws Exception {
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf())
                        .param("dateHeureResolution", "2000-01-01T00:00").param("statut", "Non affecté"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident.statut").value("Résolu"))
                .andExpect(jsonPath("$.incident.dateHeureResolution").isNotEmpty());
        em.flush();
        em.clear();
        Incident resultat = incidents.findById(incident.getId()).orElseThrow();
        assertEquals(Incident.STATUT_RESOLU, resultat.getStatut());
        assertTrue(resultat.getDateHeureResolution().isAfter(resultat.getDateHeureOuverture()));
    }

    @Test
    @WithUserDetails("agent")
    void agentNePeutPasResoudre() throws Exception {
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void incidentInexistantRetourne404JsonPourLectureEtResolution() throws Exception {
        mvc.perform(get("/api/incidents/{id}", Integer.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("Incident introuvable")));
        mvc.perform(post("/api/incidents/{id}/resoudre", Integer.MAX_VALUE).with(csrf()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void identifiantInvalideRetourne400Json() throws Exception {
        mvc.perform(get("/api/incidents/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString("entier")));
    }

    @Test
    void doubleResolutionRetourne409SansChangerDate() throws Exception {
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isOk());
        LocalDateTime premiereDate = incident.getDateHeureResolution();
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString("déjà résolu")));
        assertEquals(premiereDate, incident.getDateHeureResolution());
    }

    @Test
    void ouvertureFutureRetourne409Json() throws Exception {
        incident.setDateHeureOuverture(LocalDateTime.now().plusHours(1));
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void visiteurRecoit401JsonEtPagesWebRestentProtegeesParConnexion() throws Exception {
        mvc.perform(get("/api/incidents").with(anonymous()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/swagger-ui.html").with(anonymous())).andExpect(redirectedUrl("/connexion"));
    }

    @Test
    void absenceOuMauvaisCsrfRetourne403Json() throws Exception {
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(post("/api/incidents/{id}/resoudre", incident.getId()).with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void swaggerEtSchemaDocumententLesTroisOperationsEtErreurs() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Guardian — API des incidents"))
                .andExpect(jsonPath("$.paths['/api/incidents'].get").exists())
                .andExpect(jsonPath("$.paths['/api/incidents/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/incidents/{id}/resoudre'].post.responses['409']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.session.name").value("JSESSIONID"))
                .andExpect(jsonPath("$.paths['/incidents/{id}']").doesNotExist());
    }

    @Test
    void jetonCsrfEstAccessibleDansSessionAuthentifiee() throws Exception {
        mvc.perform(get("/api/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
