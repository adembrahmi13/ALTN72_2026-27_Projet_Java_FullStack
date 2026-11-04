package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.IncidentsParType;
import altn72.tpfilrouge.repository.NoteMoyennePrestataire;
import altn72.tpfilrouge.repository.PrestataireRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.service.StatistiquesService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
class StatistiquesTest {
    private static final LocalDateTime OUVERTURE = LocalDateTime.of(2026, 10, 1, 8, 0);

    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired PrestataireRepository prestataires;
    @Autowired EvaluationRepository evaluations;
    @Autowired StatistiquesService service;
    @Autowired EntityManager em;
    private Equipement escalator;
    private Prestataire prestataire;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station Stats"));
        escalator = equipements.save(new Equipement("EQ-STATS-1", "Escalator stats", "Escalator test", "En service", station));
        equipements.save(new Equipement("EQ-STATS-2", "Écran stats", "Écran test", "En service", station));
        prestataire = prestataires.save(new Prestataire("Prestataire stats", "stats@example.test"));
    }

    // Crée un incident résolu, affecté au prestataire, et l'évalue avec la note donnée
    private void evaluer(int note) {
        Incident incident = incidents.save(new Incident(escalator, "Panne", OUVERTURE, Incident.STATUT_RESOLU,
                prestataire.getId(), OUVERTURE.plusHours(2)));
        evaluations.save(new Evaluation(incident, note, null, LocalDate.of(2026, 10, 1)));
    }

    // Recharge depuis la base pour que les jointures JPQL voient les données des tests
    private void enregistrer() {
        em.flush();
        em.clear();
    }

    @Test
    void calculeLaNoteMoyenneDUnPrestataire() {
        evaluer(4);
        evaluer(5);
        enregistrer();

        NoteMoyennePrestataire resultat = null;
        for (NoteMoyennePrestataire ligne : service.listerNotesMoyennes()) {
            if (ligne.getNom().equals("Prestataire stats")) {
                resultat = ligne;
            }
        }
        assertNotNull(resultat);
        assertEquals(4.5, resultat.getMoyenne());
        assertEquals(2L, resultat.getNombreEvaluations());
    }

    @Test
    void compteLesIncidentsParTypeDEquipement() {
        evaluer(3);
        evaluer(3);
        enregistrer();

        Long nombreEscalator = null;
        boolean ecranPresent = false;
        for (IncidentsParType ligne : service.listerIncidentsParType()) {
            if (ligne.getType().equals("Escalator test")) {
                nombreEscalator = ligne.getNombre();
            }
            if (ligne.getType().equals("Écran test")) {
                ecranPresent = true;
            }
        }
        assertEquals(2L, nombreEscalator);
        assertFalse(ecranPresent, "Un type sans incident n'apparaît pas dans le graphique");
    }

    @Test
    void laPageAfficheLesDeuxGraphiques() throws Exception {
        evaluer(4);
        enregistrer();
        mvc.perform(get("/statistiques"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prestataire stats")))
                .andExpect(content().string(containsString("/ 5")))
                .andExpect(content().string(containsString("Escalator test")));
    }

    @Test
    @WithUserDetails("agent")
    void agentNAPasAccesAuxStatistiques() throws Exception {
        mvc.perform(get("/statistiques")).andExpect(status().isForbidden());
        mvc.perform(get("/accueil")).andExpect(content().string(not(containsString("/statistiques"))));
    }
}
