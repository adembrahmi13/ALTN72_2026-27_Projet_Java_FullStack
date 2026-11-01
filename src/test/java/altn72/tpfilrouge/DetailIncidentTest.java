package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.*;
import altn72.tpfilrouge.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(DetailIncidentTest.HorlogeTest.class)
class DetailIncidentTest {
    @TestConfiguration
    static class HorlogeTest {
        @Bean
        @Primary
        Clock horlogeFixe() {
            return Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired EvaluationRepository evaluations;
    @Autowired EntityManager entityManager;
    private Incident incident;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station F5"));
        Equipement equipement = equipements.save(
                new Equipement("EQ-F5", "Portique sortie", "Portique", "En panne", station));
        incident = incidents.save(new Incident(equipement, "Portique bloqué",
                LocalDateTime.of(2026, 10, 28, 9, 15), Incident.STATUT_NON_AFFECTE, null, null));
    }

    @Test
    @WithUserDetails("agent")
    void agentConsulteIncidentSansPrestataireNiEvaluation() throws Exception {
        mockMvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("detailIncident"))
                .andExpect(content().string(containsString("Portique bloqué")))
                .andExpect(content().string(containsString("EQ-F5")))
                .andExpect(content().string(containsString("Station F5")))
                .andExpect(content().string(containsString("2 j 2 h 45 min")))
                .andExpect(content().string(containsString("Aucun prestataire affecté.")))
                .andExpect(content().string(containsString("Aucune évaluation.")));
    }

    @Test
    @WithUserDetails("responsable")
    void responsableConsultePrestataireEtEvaluationEtDureeArreteeALaResolution() throws Exception {
        Prestataire prestataire = new Prestataire("Maintenance F5", "maintenance@example.test");
        entityManager.persist(prestataire);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut("Résolu");
        incident.setDateHeureResolution(LocalDateTime.of(2026, 10, 28, 10, 30));
        evaluations.save(new Evaluation(incident, 5, "Intervention efficace", LocalDate.of(2026, 10, 28)));
        Integer id = incident.getId();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/incidents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maintenance F5")))
                .andExpect(content().string(containsString("maintenance@example.test")))
                .andExpect(content().string(containsString("Intervention efficace")))
                .andExpect(content().string(containsString(">5</span> / 5")))
                .andExpect(content().string(containsString("28/10/2026")))
                .andExpect(content().string(containsString("0 j 1 h 15 min")));
    }

    @Test
    @WithUserDetails("responsable")
    void incidentAffecteSansEvaluation() throws Exception {
        Prestataire prestataire = new Prestataire("Dépannage F5", "01 23 45 67 89");
        entityManager.persist(prestataire);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut("Affecté");
        Integer id = incident.getId();
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/incidents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dépannage F5")))
                .andExpect(content().string(containsString("Aucune évaluation.")));
    }

    @Test
    @WithUserDetails("responsable")
    void incidentInexistantRenvoie404() throws Exception {
        mockMvc.perform(get("/incidents/{id}", Integer.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erreur"))
                .andExpect(content().string(containsString("Incident introuvable")));
    }

    @Test
    void visiteurRedirigeVersConnexion() throws Exception {
        mockMvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connexion"));
    }

    @Test
    @WithUserDetails("responsable")
    void ficheEquipementContientLienVersIncident() throws Exception {
        mockMvc.perform(get("/equipements/{id}", incident.getEquipement().getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/incidents/" + incident.getId() + "\"")));
    }
}
