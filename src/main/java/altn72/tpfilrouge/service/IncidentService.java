package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Duration;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final EquipementService equipementService;
    private final EvaluationRepository evaluationRepository;
    private final Clock horloge;

    public IncidentService(IncidentRepository incidentRepository, EquipementService equipementService,
                           EvaluationRepository evaluationRepository, Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.equipementService = equipementService;
        this.evaluationRepository = evaluationRepository;
        this.horloge = horloge;
    }

    @Transactional(readOnly = true)
    public DetailIncident consulterDetail(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", id));
        return new DetailIncident(incident, calculerDureeOuverture(incident),
                evaluationRepository.findByIncident_Id(id).orElse(null));
    }

    public String calculerDureeOuverture(Incident incident) {
        LocalDateTime fin = incident.getDateHeureResolution() != null
                ? incident.getDateHeureResolution() : LocalDateTime.now(horloge);
        Duration duree = Duration.between(incident.getDateHeureOuverture(), fin);
        return duree.toDays() + " j " + duree.toHoursPart() + " h " + duree.toMinutesPart() + " min";
    }

    // RG1 : on ne reprend que la description saisie, le reste est fixé ici pour qu'un formulaire trafiqué ne puisse rien imposer
    public Incident declarerIncident(Integer equipementId, String description) {
        Equipement equipement = equipementService.trouverEquipement(equipementId);
        return incidentRepository.save(new Incident(equipement, description, LocalDateTime.now(horloge), Incident.STATUT_NON_AFFECTE, null, null));
    }
}
