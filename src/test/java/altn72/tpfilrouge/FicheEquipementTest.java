package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("responsable")
@Transactional
class FicheEquipementTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private EquipementRepository equipementRepository;
    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void ficheAfficheLesIncidentsDuPlusRecentAuPlusAncien() throws Exception {
        Station station = stationRepository.save(new Station("Station test"));
        Equipement equipement = equipementRepository.save(new Equipement("EQ-TEST", "Ascenseur quai 1", "Ascenseur", "En panne", station));
        LocalDateTime maintenant = LocalDateTime.now();
        incidentRepository.saveAll(List.of(
                new Incident(equipement, "Ancien", maintenant.minusDays(10), "Résolu", null, maintenant.minusDays(9)),
                new Incident(equipement, "Récent", maintenant.minusHours(2), "Non affecté", null, null)));

        mockMvc.perform(get("/equipements/{id}", equipement.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("ficheEquipement"))
                .andExpect(model().attribute("incidents", contains(
                        hasProperty("description", is("Récent")),
                        hasProperty("description", is("Ancien")))));
    }

    @Test
    void equipementInexistantAfficheLaPageErreurEn404() throws Exception {
        mockMvc.perform(get("/equipements/{id}", Integer.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erreur"));
    }

    // Évolution "Équipement fragile" : badge affiché si et seulement si l'équipement est fragile
    private Equipement equipementAvecIncidentsRecents(String reference, int nombre) {
        Station station = stationRepository.save(new Station("Station " + reference));
        Equipement equipement = equipementRepository.save(new Equipement(reference, "Portique", "Portique", "En panne", station));
        for (int i = 0; i < nombre; i++) {
            incidentRepository.save(new Incident(equipement, "Panne " + i, LocalDateTime.now().minusDays(1), "Non affecté", null, null));
        }
        return equipement;
    }

    @Test
    void badgeFragileAfficheSurEquipementFragile() throws Exception {
        Equipement equipement = equipementAvecIncidentsRecents("EQ-FRAG", 3);

        mockMvc.perform(get("/equipements/{id}", equipement.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Équipement fragile")));
    }

    @Test
    void badgeFragileAbsentSurEquipementNonFragile() throws Exception {
        Equipement equipement = equipementAvecIncidentsRecents("EQ-SOLIDE", 2);

        mockMvc.perform(get("/equipements/{id}", equipement.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Équipement fragile"))));
    }
}
