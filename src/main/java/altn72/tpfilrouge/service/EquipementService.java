package altn72.tpfilrouge.service;

import org.springframework.stereotype.Service;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import java.util.List;

// La classe EquipementService est annotée avec @Service pour indiquer à Spring qu'il s'agit d'un service métier.
@Service
public class EquipementService {
    private final EquipementRepository equipementRepository;
    private final IncidentRepository incidentRepository;

    public EquipementService(EquipementRepository equipementRepository, IncidentRepository incidentRepository) {
        this.equipementRepository = equipementRepository;
        this.incidentRepository = incidentRepository;
    }

    public List<Equipement> getEquipementsByStationId(Integer stationId) {
        return equipementRepository.findByStation_Id(stationId);
    }

    public Equipement trouverEquipement(Integer id) {
        return equipementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Équipement", id));
    }

    public List<Incident> listerIncidents(Integer equipementId) {
        return incidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc(equipementId);
    }

}
