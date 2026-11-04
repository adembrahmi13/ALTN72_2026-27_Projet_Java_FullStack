package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import altn72.tpfilrouge.repository.StationRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
@Import(EvaluationPrestataireTest.HorlogeTest.class)
class EvaluationPrestataireTest {
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 4, 12, 0);

    @TestConfiguration
    static class HorlogeTest {
        @Bean
        @Primary
        Clock horlogeEvaluation() {
            return Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired PrestataireRepository prestataires;
    @Autowired EvaluationRepository evaluations;
    @Autowired EntityManager em;
    private Incident incident;

    // Cas nominal de RG8 : incident résolu et affecté à un prestataire
    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station F10"));
        Equipement equipement = equipements.save(new Equipement("EQ-F10", "Escalator F10", "Escalier mécanique", "En service", station));
        Prestataire prestataire = prestataires.save(new Prestataire("Maintenance F10", "f10@example.test"));
        incident = incidents.save(new Incident(equipement, "Escalator arrêté", MAINTENANT.minusDays(2),
                Incident.STATUT_RESOLU, prestataire.getId(), MAINTENANT.minusDays(1)));
        // Recharge l'incident comme le ferait une vraie requête, pour que la relation prestataire soit remplie
        em.flush();
        em.clear();
        incident = incidents.findById(incident.getId()).orElseThrow();
    }

    private ResultActions evaluer(Integer incidentId, String note) throws Exception {
        return mvc.perform(post("/incidents/{id}/evaluer", incidentId).with(csrf())
                .param("note", note).param("commentaire", "Intervention rapide"));
    }

    @Test
    void responsableEvalueUnIncidentResoluEtAffecte() throws Exception {
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(containsString("/incidents/" + incident.getId() + "/evaluer")));
        evaluer(incident.getId(), "4").andExpect(redirectedUrl("/incidents/" + incident.getId()));

        Evaluation evaluation = evaluations.findByIncident_Id(incident.getId()).orElseThrow();
        assertEquals(4, evaluation.getNote());
        assertEquals("Intervention rapide", evaluation.getCommentaire());
        assertEquals(LocalDate.of(2026, 11, 4), evaluation.getDate());
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(not(containsString("/evaluer"))));
    }

    @Test
    void incidentNonResoluRefuse() throws Exception {
        incident.setStatut(Incident.STATUT_AFFECTE);
        incident.setDateHeureResolution(null);
        evaluer(incident.getId(), "4").andExpect(status().isConflict());
        assertFalse(evaluations.existsByIncident_Id(incident.getId()));
    }

    @Test
    void incidentResoluSansPrestataireRefuse() throws Exception {
        incident.setPrestataireId(null);
        evaluer(incident.getId(), "4").andExpect(status().isConflict());
        assertFalse(evaluations.existsByIncident_Id(incident.getId()));
    }

    @Test
    void uneSeuleEvaluationParIncident() throws Exception {
        evaluer(incident.getId(), "4").andExpect(status().is3xxRedirection());
        evaluer(incident.getId(), "1")
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("déjà été évalué")));
        assertEquals(4, evaluations.findByIncident_Id(incident.getId()).orElseThrow().getNote());
    }

    @Test
    void noteHorsDeUnACinqRefusee() throws Exception {
        for (String noteInvalide : new String[]{"0", "6", ""}) {
            evaluer(incident.getId(), noteInvalide)
                    .andExpect(status().isOk())
                    .andExpect(view().name("evaluerIncident"));
        }
        assertFalse(evaluations.existsByIncident_Id(incident.getId()));
    }

    @Test
    @WithUserDetails("agent")
    void agentNeVoitPasLeLienEtNePeutPasEvaluerParUrl() throws Exception {
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(not(containsString("/evaluer"))));
        evaluer(incident.getId(), "4").andExpect(status().isForbidden());
        assertFalse(evaluations.existsByIncident_Id(incident.getId()));
    }

    @Test
    void incidentInexistantRenvoie404() throws Exception {
        evaluer(Integer.MAX_VALUE, "4").andExpect(status().isNotFound());
    }
}
