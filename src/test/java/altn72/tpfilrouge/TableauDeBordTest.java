package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.NiveauAttention;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.IncidentsActifsParStation;
import altn72.tpfilrouge.repository.PrestataireRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.service.IncidentActif;
import altn72.tpfilrouge.service.TableauDeBordService;
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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
@Import(TableauDeBordTest.HorlogeTest.class)
class TableauDeBordTest {
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    // Horloge figée pour tester les seuils à la minute près
    @TestConfiguration
    static class HorlogeTest {
        @Bean
        @Primary
        Clock horlogeTableauDeBord() {
            return Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired PrestataireRepository prestataires;
    @Autowired TableauDeBordService service;
    private Station station;
    private Equipement equipement;

    @BeforeEach
    void preparer() {
        station = stations.save(new Station("Station F11"));
        equipement = equipements.save(new Equipement("EQ-F11", "Portique F11", "Portique", "En panne", station));
    }

    // Crée un incident non affecté ouvert depuis le nombre de minutes donné
    private Incident incidentOuvertDepuis(long minutes) {
        return incidents.save(new Incident(equipement, "Panne", MAINTENANT.minusMinutes(minutes),
                Incident.STATUT_NON_AFFECTE, null, null));
    }

    @Test
    void ouvertDepuis47HeuresEstVert() {
        assertEquals(NiveauAttention.VERT, service.calculerNiveau(incidentOuvertDepuis(47 * 60)));
        assertEquals(NiveauAttention.VERT, service.calculerNiveau(incidentOuvertDepuis(48 * 60 - 1)));
    }

    @Test
    void aPartirDe48HeuresDevientOrange() {
        assertEquals(NiveauAttention.ORANGE, service.calculerNiveau(incidentOuvertDepuis(48 * 60)));
        assertEquals(NiveauAttention.ORANGE, service.calculerNiveau(incidentOuvertDepuis(72 * 60 - 1)));
    }

    @Test
    void aPartirDe72HeuresDevientRouge() {
        assertEquals(NiveauAttention.ROUGE, service.calculerNiveau(incidentOuvertDepuis(72 * 60)));
    }

    @Test
    void incidentAffecteAUnEtatDistinctMemeTresAncien() {
        Prestataire prestataire = prestataires.save(new Prestataire("Maintenance F11", "f11@example.test"));
        Incident incident = incidentOuvertDepuis(200 * 60);
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut(Incident.STATUT_AFFECTE);
        assertEquals(NiveauAttention.AFFECTE, service.calculerNiveau(incident));
    }

    @Test
    void incidentResoluNApparaitPlusParmiLesActifs() {
        Incident actif = incidentOuvertDepuis(60);
        Incident resolu = incidentOuvertDepuis(120);
        resolu.setStatut(Incident.STATUT_RESOLU);
        resolu.setDateHeureResolution(MAINTENANT);

        List<IncidentActif> resultat = service.listerIncidentsActifs(station.getId(), null);
        assertEquals(1, resultat.size());
        assertEquals(actif.getId(), resultat.get(0).incident().getId());
    }

    @Test
    void filtreParStationEtParNiveau() throws Exception {
        incidentOuvertDepuis(60);
        Incident rouge = incidentOuvertDepuis(80 * 60);

        List<IncidentActif> rouges = service.listerIncidentsActifs(station.getId(), NiveauAttention.ROUGE);
        assertEquals(1, rouges.size());
        assertEquals(rouge.getId(), rouges.get(0).incident().getId());

        mvc.perform(get("/tableau-de-bord").param("stationId", station.getId().toString()).param("niveau", "ROUGE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/incidents/" + rouge.getId())));
    }

    @Test
    void compteLesIncidentsActifsParStationAvecLaRequeteNative() {
        incidentOuvertDepuis(60);
        incidentOuvertDepuis(120);
        Incident resolu = incidentOuvertDepuis(180);
        resolu.setStatut(Incident.STATUT_RESOLU);
        resolu.setDateHeureResolution(MAINTENANT);
        incidents.flush();

        Long nombre = null;
        for (IncidentsActifsParStation ligne : service.compterIncidentsActifsParStation()) {
            if (ligne.getNom().equals("Station F11")) {
                nombre = ligne.getNombre();
            }
        }
        assertEquals(2L, nombre);
    }

    @Test
    void niveauInvalideDansLAdresseAfficheUnMessageClair() throws Exception {
        mvc.perform(get("/tableau-de-bord").param("niveau", "VIOLET"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("est invalide")));
    }

    @Test
    @WithUserDetails("agent")
    void agentNAPasAccesAuTableauDeBord() throws Exception {
        mvc.perform(get("/tableau-de-bord")).andExpect(status().isForbidden());
        mvc.perform(get("/accueil")).andExpect(content().string(not(containsString("/tableau-de-bord"))));
    }
}
