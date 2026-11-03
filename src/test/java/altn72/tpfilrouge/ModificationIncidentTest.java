package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.formulaire.ModificationIncident;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.service.IncidentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("agent")
class ModificationIncidentTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired IncidentService service;
    @Autowired EntityManager em;
    Incident incident;
    Equipement autre;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station F6"));
        Station autreStation = stations.save(new Station("Autre station F6"));
        Equipement equipement = equipements.save(new Equipement("F6-A", "Portique", "Portique", "En panne", station));
        autre = equipements.save(new Equipement("F6-B", "Borne", "Borne", "En panne", autreStation));
        incident = incidents.save(new Incident(equipement, "Description initiale",
                LocalDateTime.now().minusHours(3).withNano(0), Incident.STATUT_NON_AFFECTE, null, null));
    }

    @Test
    void agentVoitFormulairePrefilledSansSelecteurEquipement() throws Exception {
        mvc.perform(get("/incidents/{id}/modifier", incident.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Description initiale")))
                .andExpect(content().string(not(containsString("<select"))));
    }

    @Test
    @WithUserDetails("responsable")
    void responsableVoitEquipementsEtStations() throws Exception {
        mvc.perform(get("/incidents/{id}/modifier", incident.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<select")))
                .andExpect(content().string(containsString("Autre station F6")));
    }

    @Test
    void agentCorrigeDescriptionEtIgnoreChampsNonAutorises() throws Exception {
        LocalDateTime ouverture = incident.getDateHeureOuverture();
        Integer equipementId = incident.getEquipement().getId();
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "  Description corrigée  ")
                        .param("equipementId", equipementId.toString())
                        .param("statut", "Résolu").param("prestataireId", "999")
                        .param("id", "999").param("dateHeureOuverture", "2000-01-01T00:00")
                        .param("dateHeureResolution", "2026-11-03T12:00")
                        .param("profil", "RESPONSABLE_EXPLOITATION"))
                .andExpect(redirectedUrl("/incidents/" + incident.getId()))
                .andExpect(flash().attributeExists("messageSucces"));
        em.flush();
        em.clear();
        Incident resultat = incidents.findById(incident.getId()).orElseThrow();
        assertEquals("Description corrigée", resultat.getDescription());
        assertEquals(equipementId, resultat.getEquipement().getId());
        assertEquals(ouverture, resultat.getDateHeureOuverture());
        assertEquals(Incident.STATUT_NON_AFFECTE, resultat.getStatut());
        assertNull(resultat.getPrestataireId());
        assertNull(resultat.getDateHeureResolution());
    }

    @Test
    void agentNePeutPasChangerEquipementMemeParRequeteDirecte() throws Exception {
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Tentative").param("equipementId", autre.getId().toString()))
                .andExpect(status().isForbidden());
        assertEquals("Description initiale", incident.getDescription());
        assertNotEquals(autre.getId(), incident.getEquipement().getId());
    }

    @Test
    @WithUserDetails("responsable")
    void responsableCorrigeEquipementEtStationAvantAffectation() throws Exception {
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Mauvais équipement corrigé").param("equipementId", autre.getId().toString()))
                .andExpect(status().is3xxRedirection());
        em.flush();
        em.clear();
        Incident resultat = incidents.findById(incident.getId()).orElseThrow();
        assertEquals(autre.getId(), resultat.getEquipement().getId());
        assertEquals("Autre station F6", resultat.getEquipement().getStation().getNom());
    }

    private void affecter() {
        Prestataire prestataire = new Prestataire("Prestataire F6", "contact@example.test");
        em.persist(prestataire);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut("Affecté");
        em.flush();
    }

    @Test
    @WithUserDetails("responsable")
    void responsableNeChangePasEquipementApresAffectation() throws Exception {
        affecter();
        mvc.perform(get("/incidents/{id}/modifier", incident.getId()))
                .andExpect(content().string(not(containsString("<select"))));
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Tentative").param("equipementId", autre.getId().toString()))
                .andExpect(status().isForbidden());
        assertEquals("Description initiale", incident.getDescription());
    }

    @Test
    void agentPeutPreciserDescriptionApresAffectation() throws Exception {
        affecter();
        Integer prestataireId = incident.getPrestataireId();
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Précision après affectation")
                        .param("equipementId", incident.getEquipement().getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertEquals("Précision après affectation", incident.getDescription());
        assertEquals(prestataireId, incident.getPrestataireId());
    }

    @Test
    void descriptionVideOuTropLongueReafficheFormulaireSansModifier() throws Exception {
        for (String description : new String[]{"   ", "x".repeat(256)}) {
            mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                            .param("description", description)
                            .param("equipementId", incident.getEquipement().getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("modifierIncident"))
                    .andExpect(model().attributeHasFieldErrors("modificationIncident", "description"));
        }
        assertEquals("Description initiale", incident.getDescription());
    }

    @Test
    @WithUserDetails("responsable")
    void equipementInexistantRefuseSansModificationPartielle() throws Exception {
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Tentative").param("equipementId", Integer.toString(Integer.MAX_VALUE)))
                .andExpect(status().isNotFound());
        assertEquals("Description initiale", incident.getDescription());
    }

    @Test
    void incidentInexistantRenvoie404PourLectureEtEcriture() throws Exception {
        mvc.perform(get("/incidents/{id}/modifier", Integer.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(post("/incidents/{id}/modifier", Integer.MAX_VALUE).with(csrf())
                        .param("description", "Description").param("equipementId", autre.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("responsable")
    void incidentResoluEstFigeMemePourResponsable() throws Exception {
        incident.setStatut("Résolu");
        incident.setDateHeureResolution(LocalDateTime.now());
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(not(containsString("Modifier l&#39;incident"))))
                .andExpect(content().string(not(containsString("/modifier"))));
        mvc.perform(get("/incidents/{id}/modifier", incident.getId())).andExpect(status().isConflict());
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf())
                        .param("description", "Tentative")
                        .param("equipementId", incident.getEquipement().getId().toString()))
                .andExpect(status().isConflict());
        assertEquals("Description initiale", incident.getDescription());
    }

    @Test
    void visiteurEtRequeteSansCsrfSontRefuses() throws Exception {
        mvc.perform(get("/incidents/{id}/modifier", incident.getId()).with(anonymous()))
                .andExpect(redirectedUrl("/connexion"));
        mvc.perform(post("/incidents/{id}/modifier", incident.getId()).with(csrf()).with(anonymous())
                        .param("description", "Tentative").param("equipementId", autre.getId().toString()))
                .andExpect(redirectedUrl("/connexion"));
        mvc.perform(post("/incidents/{id}/modifier", incident.getId())
                        .param("description", "Tentative").param("equipementId", autre.getId().toString()))
                .andExpect(status().isForbidden());
        assertEquals("Description initiale", incident.getDescription());
    }

    @Test
    @WithMockUser(roles = "AUTRE")
    void roleInconnuRefuseDansService() {
        ModificationIncident formulaire = new ModificationIncident();
        formulaire.setDescription("Tentative");
        formulaire.setEquipementId(autre.getId());
        assertThrows(AccessDeniedException.class, () -> service.modifierIncident(incident.getId(), formulaire));
    }
}
