package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("responsable")
@Transactional
class DeclarationIncidentTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private EquipementRepository equipementRepository;
    @Autowired
    private IncidentRepository incidentRepository;

    private Equipement equipement;

    @BeforeEach
    void creerEquipement() {
        Station station = stationRepository.save(new Station("Station test"));
        equipement = equipementRepository.save(new Equipement("EQ-F4", "Portique sortie", "Portique", "En service", station));
    }

    @Test
    void incidentDeclareDemarreNonAffecteSansPrestataire() throws Exception {
        mockMvc.perform(post("/equipements/{id}/incidents", equipement.getId()).with(csrf())
                        .param("description", "Portique bloqué")
                        .param("prestataireId", "1")
                        .param("statut", "Résolu"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipements/" + equipement.getId()));

        List<Incident> incidents = incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(equipement.getId());
        assertEquals(1, incidents.size());
        Incident incident = incidents.get(0);
        assertEquals("Portique bloqué", incident.getDescription());
        assertEquals(Incident.STATUT_NON_AFFECTE, incident.getStatut());
        assertNotNull(incident.getDateHeureOuverture());
        assertNull(incident.getPrestataireId());
        assertNull(incident.getDateHeureResolution());
    }

    @Test
    void descriptionVideReafficheLeFormulaireAvecUnMessage() throws Exception {
        mockMvc.perform(post("/equipements/{id}/incidents", equipement.getId()).with(csrf())
                        .param("description", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("nouvelIncident"))
                .andExpect(model().attributeHasFieldErrors("nouvelIncident", "description"));

        assertTrue(incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(equipement.getId()).isEmpty());
    }

    @Test
    void declarationSurEquipementInexistantRenvoie404() throws Exception {
        mockMvc.perform(post("/equipements/{id}/incidents", Integer.MAX_VALUE).with(csrf())
                        .param("description", "Panne"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erreur"));
    }
}
