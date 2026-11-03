package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.service.IncidentService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
@Import(ResolutionIncidentTest.HorlogeTest.class)
class ResolutionIncidentTest {
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 3, 12, 0);

    @TestConfiguration
    static class HorlogeTest {
        @Bean
        @Primary
        Clock horlogeResolution() {
            return Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired IncidentService service;
    @Autowired EntityManager em;
    private Incident incident;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station F8"));
        Equipement equipement = equipements.save(new Equipement("EQ-F8", "Portique F8", "Portique", "En panne", station));
        incident = incidents.save(new Incident(equipement, "Portique bloqué",
                MAINTENANT.minusHours(3), Incident.STATUT_NON_AFFECTE, null, null));
    }

    @Test
    void responsableResoutSansPrestataireAvecDateServeurEtDureeFigee() throws Exception {
        Integer id = incident.getId();
        mvc.perform(get("/incidents/{id}", id))
                .andExpect(content().string(containsString("/incidents/" + id + "/resoudre")));
        mvc.perform(post("/incidents/{id}/resoudre", id).with(csrf())
                        .param("dateHeureResolution", "2000-01-01T00:00")
                        .param("statut", "Non affecté")
                        .param("description", "Tentative")
                        .param("prestataireId", "999"))
                .andExpect(redirectedUrl("/incidents/" + id))
                .andExpect(flash().attributeExists("messageSucces"));
        em.flush();
        em.clear();
        Incident resultat = incidents.findById(id).orElseThrow();
        assertEquals(Incident.STATUT_RESOLU, resultat.getStatut());
        assertEquals(MAINTENANT, resultat.getDateHeureResolution());
        assertEquals(MAINTENANT.minusHours(3), resultat.getDateHeureOuverture());
        assertEquals("Portique bloqué", resultat.getDescription());
        assertNull(resultat.getPrestataireId());
        mvc.perform(get("/incidents/{id}", id))
                .andExpect(content().string(containsString("03/11/2026 12:00")))
                .andExpect(content().string(containsString("0 j 3 h 0 min")))
                .andExpect(content().string(not(containsString("/resoudre"))))
                .andExpect(content().string(not(containsString("/modifier"))));
        mvc.perform(get("/equipements/{id}", resultat.getEquipement().getId()))
                .andExpect(content().string(containsString("Résolu")))
                .andExpect(content().string(containsString("03/11/2026 12:00")));
    }

    @Test
    void responsableResoutAvecPrestataireSansPerdreAffectation() throws Exception {
        Prestataire prestataire = new Prestataire("Maintenance F8", "maintenance@example.test");
        em.persist(prestataire);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut("Affecté");
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        em.flush();
        assertEquals(prestataire.getId(), incident.getPrestataireId());
        assertEquals(Incident.STATUT_RESOLU, incident.getStatut());
        assertEquals(MAINTENANT, incident.getDateHeureResolution());
    }

    @Test
    void resolutionEgaleAOuvertureAcceptee() throws Exception {
        incident.setDateHeureOuverture(MAINTENANT);
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(MAINTENANT, incident.getDateHeureResolution());
    }

    @Test
    void ouvertureFutureRefuseeSansModification() throws Exception {
        incident.setDateHeureOuverture(MAINTENANT.plusMinutes(1));
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("antérieure")));
        assertEquals(Incident.STATUT_NON_AFFECTE, incident.getStatut());
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void doubleResolutionConservePremiereDate() throws Exception {
        LocalDateTime premiereResolution = MAINTENANT.minusHours(1);
        incident.setStatut(Incident.STATUT_RESOLU);
        incident.setDateHeureResolution(premiereResolution);
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("déjà résolu")));
        assertEquals(premiereResolution, incident.getDateHeureResolution());
    }

    @Test
    void statutResoluSansDateNePeutPasEtreRecloture() throws Exception {
        incident.setStatut(Incident.STATUT_RESOLU);
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isConflict());
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    @WithUserDetails("agent")
    void agentNeVoitPasActionEtNePeutPasResoudreParUrlOuService() throws Exception {
        assertThrows(AccessDeniedException.class, () -> service.resoudreIncident(incident.getId()));
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(not(containsString("/resoudre"))));
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()))
                .andExpect(status().isForbidden());
        assertEquals(Incident.STATUT_NON_AFFECTE, incident.getStatut());
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void incidentInexistantRenvoie404() throws Exception {
        mvc.perform(post("/incidents/{id}/resoudre", Integer.MAX_VALUE).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void visiteurRedirigeVersConnexion() throws Exception {
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()).with(csrf()).with(anonymous()))
                .andExpect(redirectedUrl("/connexion"));
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void absenceCsrfRefusee() throws Exception {
        mvc.perform(post("/incidents/{id}/resoudre", incident.getId()))
                .andExpect(status().isForbidden());
        assertNull(incident.getDateHeureResolution());
    }
}
