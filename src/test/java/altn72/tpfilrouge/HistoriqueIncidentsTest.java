package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
class HistoriqueIncidentsTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired EntityManager em;
    private Equipement equipement;
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 11, 1, 12, 0);

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station historique"));
        equipement = equipements.save(new Equipement("EQ-F9", "Borne quai", "Borne", "En service", station));
    }

    private Incident creer(String description, LocalDateTime ouverture, String statut, LocalDateTime resolution) {
        return incidents.save(new Incident(equipement, description, ouverture, statut, null, resolution));
    }

    @Test
    void triParResolutionDecroissanteEtExclusionDesIncidentsOuverts() throws Exception {
        // L'ordre des ouvertures est volontairement inverse de celui des résolutions.
        Incident ancien = creer("Ancien résolu", DATE.minusDays(2), Incident.STATUT_RESOLU, DATE);
        Incident recent = creer("Récent résolu", DATE.minusDays(5), Incident.STATUT_RESOLU, DATE.plusDays(1));
        creer("Toujours ouvert", DATE, Incident.STATUT_NON_AFFECTE, null);
        creer("Encore affecté", DATE, "Affecté", null);
        creer("Résolu sans date", DATE, Incident.STATUT_RESOLU, null);
        creer("Date sans statut résolu", DATE, "Affecté", DATE.plusDays(2));

        mvc.perform(get("/incidents/historique"))
                .andExpect(status().isOk())
                .andExpect(view().name("historiqueIncidents"))
                .andExpect(model().attribute("incidents", contains(
                        hasProperty("id", is(recent.getId())), hasProperty("id", is(ancien.getId())))))
                .andExpect(content().string(containsString("Station historique")))
                .andExpect(content().string(containsString("EQ-F9")))
                .andExpect(content().string(containsString("02/11/2026 12:00")))
                .andExpect(content().string(containsString("Sans prestataire")))
                .andExpect(content().string(containsString("href=\"/incidents/" + recent.getId() + "\"")))
                .andExpect(content().string(not(containsString("Toujours ouvert"))));
    }

    @Test
    @WithUserDetails("agent")
    void agentConsulteHistoriqueAvecPrestataire() throws Exception {
        Prestataire prestataire = new Prestataire("Maintenance historique", "contact@example.test");
        em.persist(prestataire);
        Incident incident = creer("Résolu avec prestataire", DATE.minusHours(2), Incident.STATUT_RESOLU, DATE);
        incident.setPrestataireId(prestataire.getId());
        em.flush();
        em.clear();
        mvc.perform(get("/incidents/historique"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maintenance historique")))
                .andExpect(content().string(containsString("Résolu avec prestataire")));
    }

    @Test
    void historiqueVideAfficheMessageSansTableau() throws Exception {
        mvc.perform(get("/incidents/historique"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("incidents", empty()))
                .andExpect(content().string(containsString("Aucun incident résolu pour le moment.")))
                .andExpect(content().string(not(containsString("<table>"))));
    }

    @Test
    void visiteurEstRedirigeVersConnexion() throws Exception {
        mvc.perform(get("/incidents/historique").with(anonymous()))
                .andExpect(redirectedUrl("/connexion"));
    }

    @Test
    void resolutionF8FaitApparaitreIncidentDansHistorique() throws Exception {
        Incident incident = creer("Clôturé depuis F8", LocalDateTime.now().minusDays(1), Incident.STATUT_NON_AFFECTE, null);
        mvc.perform(get("/incidents/historique"))
                .andExpect(model().attribute("incidents", empty()));
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/incidents/historique"))
                .andExpect(model().attribute("incidents", contains(hasProperty("id", is(incident.getId())))))
                .andExpect(content().string(containsString("Clôturé depuis F8")));
    }

    @Test
    void datesEgalesOntUnOrdreStableParIdentifiantDecroissant() throws Exception {
        Incident premier = creer("Premier", DATE.minusDays(1), Incident.STATUT_RESOLU, DATE);
        Incident second = creer("Second", DATE.minusDays(1), Incident.STATUT_RESOLU, DATE);
        mvc.perform(get("/incidents/historique"))
                .andExpect(model().attribute("incidents", contains(
                        hasProperty("id", is(second.getId())), hasProperty("id", is(premier.getId())))));
    }
}
