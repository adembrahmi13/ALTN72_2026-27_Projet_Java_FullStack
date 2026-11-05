package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.service.EquipementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

// Cas limites de l'évolution "Équipement fragile" (EF1 à EF4)
@SpringBootTest
@Transactional
@Import(EquipementFragileTest.HorlogeTest.class)
class EquipementFragileTest {
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 5, 12, 0);

    // Horloge figée pour tester la fenêtre de 30 jours à la minute près
    @TestConfiguration
    static class HorlogeTest {
        @Bean
        @Primary
        Clock horlogeEquipementFragile() {
            return Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired EquipementService service;
    private Station station;
    private Equipement equipement;

    @BeforeEach
    void preparer() {
        station = stations.save(new Station("Station EF"));
        equipement = equipements.save(new Equipement("EQ-EF1", "Portique EF1", "Portique", "En panne", station));
    }

    private void incident(Equipement cible, LocalDateTime ouverture, String statut) {
        incidents.save(new Incident(cible, "Panne", ouverture, statut, null,
                Incident.STATUT_RESOLU.equals(statut) ? MAINTENANT : null));
    }

    private void incidentsRecents(int nombre) {
        for (int i = 0; i < nombre; i++) {
            incident(equipement, MAINTENANT.minusDays(1), Incident.STATUT_NON_AFFECTE);
        }
    }

    @Test
    void deuxIncidentsNonFragile() {
        incidentsRecents(2);
        assertFalse(service.estFragile(equipement.getId()));
    }

    @Test
    void troisIncidentsFragile() {
        incidentsRecents(3);
        assertTrue(service.estFragile(equipement.getId()));
    }

    @Test
    void troisIncidentsDontUnResoluFragile() {
        incidentsRecents(2);
        incident(equipement, MAINTENANT.minusDays(2), Incident.STATUT_RESOLU);
        assertTrue(service.estFragile(equipement.getId()));
    }

    @Test
    void incidentA30JoursEt1MinuteNonCompte() {
        incidentsRecents(2);
        incident(equipement, MAINTENANT.minusDays(30).minusMinutes(1), Incident.STATUT_NON_AFFECTE);
        assertFalse(service.estFragile(equipement.getId()));
    }

    @Test
    void incidentA29Jours23h59Compte() {
        incidentsRecents(2);
        incident(equipement, MAINTENANT.minusDays(30).plusMinutes(1), Incident.STATUT_NON_AFFECTE);
        assertTrue(service.estFragile(equipement.getId()));
    }

    @Test
    void vieuxIncidentsNonComptes() {
        incidentsRecents(2);
        for (int i = 0; i < 5; i++) {
            incident(equipement, MAINTENANT.minusDays(45), Incident.STATUT_RESOLU);
        }
        assertFalse(service.estFragile(equipement.getId()));
    }

    @Test
    void equipementVoisinNonContamine() {
        Equipement voisin = equipements.save(new Equipement("EQ-EF2", "Portique EF2", "Portique", "En service", station));
        incidentsRecents(3);
        incident(voisin, MAINTENANT.minusDays(1), Incident.STATUT_NON_AFFECTE);
        assertTrue(service.estFragile(equipement.getId()));
        assertFalse(service.estFragile(voisin.getId()));
    }
}
