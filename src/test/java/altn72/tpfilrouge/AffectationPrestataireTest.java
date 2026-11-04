package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
class AffectationPrestataireTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired PrestataireRepository prestataires;
    private Incident incident;
    private Prestataire premier;
    private Prestataire second;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station F7"));
        Equipement equipement = equipements.save(new Equipement("EQ-F7", "Ascenseur F7", "Ascenseur", "En panne", station));
        incident = incidents.save(new Incident(equipement, "Ascenseur bloqué",
                LocalDateTime.now().minusHours(5), Incident.STATUT_NON_AFFECTE, null, null));
        premier = prestataires.save(new Prestataire("Maintenance F7", "f7@example.test"));
        second = prestataires.save(new Prestataire("Dépannage F7", "01 02 03 04 05"));
    }

    private ResultActions affecter(Integer incidentId, Integer prestataireId) throws Exception {
        return mvc.perform(post("/incidents/{id}/affecter-prestataire", incidentId).with(csrf())
                .param("prestataireId", String.valueOf(prestataireId)));
    }

    @Test
    void responsableAffectePuisReaffecte() throws Exception {
        affecter(incident.getId(), premier.getId()).andExpect(redirectedUrl("/incidents/" + incident.getId()));
        assertEquals(premier.getId(), incident.getPrestataireId());
        assertEquals(Incident.STATUT_AFFECTE, incident.getStatut());

        affecter(incident.getId(), second.getId()).andExpect(status().is3xxRedirection());
        assertEquals(second.getId(), incident.getPrestataireId());
    }

    @Test
    void incidentResoluNePeutPasEtreAffecte() throws Exception {
        incident.setStatut(Incident.STATUT_RESOLU);
        incident.setDateHeureResolution(LocalDateTime.now());
        affecter(incident.getId(), premier.getId())
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("incident résolu")));
        assertNull(incident.getPrestataireId());
    }

    @Test
    @WithUserDetails("agent")
    void agentNeVoitPasLeFormulaireEtNePeutPasAffecterParUrl() throws Exception {
        mvc.perform(get("/incidents/{id}", incident.getId()))
                .andExpect(content().string(not(containsString("/affecter-prestataire"))));
        affecter(incident.getId(), premier.getId()).andExpect(status().isForbidden());
        assertNull(incident.getPrestataireId());
    }

    @Test
    void incidentOuPrestataireInexistantRenvoie404() throws Exception {
        affecter(Integer.MAX_VALUE, premier.getId()).andExpect(status().isNotFound());
        affecter(incident.getId(), Integer.MAX_VALUE).andExpect(status().isNotFound());
        assertNull(incident.getPrestataireId());
    }
}
