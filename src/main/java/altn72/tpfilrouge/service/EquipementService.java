package altn72.tpfilrouge.service;

import org.springframework.stereotype.Service;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

// La classe EquipementService est annotée avec @Service pour indiquer à Spring qu'il s'agit d'un service métier.
@Service
public class EquipementService {
    // EF5 : seuil et fenêtre de la règle "équipement fragile", définis uniquement ici
    public static final int SEUIL_FRAGILE = 3;
    public static final int FENETRE_FRAGILE_JOURS = 30;

    private final EquipementRepository equipementRepository;
    private final IncidentRepository incidentRepository;
    private final Clock horloge;

    public EquipementService(EquipementRepository equipementRepository, IncidentRepository incidentRepository, Clock horloge) {
        this.equipementRepository = equipementRepository;
        this.incidentRepository = incidentRepository;
        this.horloge = horloge;
    }

    public List<Equipement> getEquipementsByStationId(Integer stationId) {
        return equipementRepository.findByStation_Id(stationId);
    }

    public Equipement trouverEquipement(Integer id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Équipement", id));
    }

    public List<Equipement> listerEquipements() {
        return equipementRepository.findAll(org.springframework.data.domain.Sort.by("referenceUnique"));
    }

    public List<Incident> listerIncidents(Integer equipementId) {
        return incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(equipementId);
    }

    // EF1 à EF4 : recalculé à chaque appel sur une fenêtre glissante, jamais enregistré en base
    public boolean estFragile(Integer equipementId) {
        LocalDateTime debutFenetre = LocalDateTime.now(horloge).minusDays(FENETRE_FRAGILE_JOURS);
        return incidentRepository.countByEquipement_IdAndDateHeureOuvertureGreaterThanEqual(equipementId, debutFenetre)
                >= SEUIL_FRAGILE;
    }

}
