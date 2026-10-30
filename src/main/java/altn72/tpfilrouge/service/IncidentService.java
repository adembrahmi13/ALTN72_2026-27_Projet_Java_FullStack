package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final EquipementService equipementService;

    public IncidentService(IncidentRepository incidentRepository, EquipementService equipementService) {
        this.incidentRepository = incidentRepository;
        this.equipementService = equipementService;
    }

    // RG1 : on ne reprend que la description saisie, le reste est fixé ici pour qu'un formulaire trafiqué ne puisse rien imposer
    public Incident declarerIncident(Integer equipementId, String description) {
        Equipement equipement = equipementService.trouverEquipement(equipementId);
        return incidentRepository.save(new Incident(equipement, description, LocalDateTime.now(), Incident.STATUT_NON_AFFECTE, null, null));
    }
}
