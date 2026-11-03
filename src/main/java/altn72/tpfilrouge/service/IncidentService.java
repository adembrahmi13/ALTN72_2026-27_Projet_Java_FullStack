package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.repository.EvaluationRepository;
import org.springframework.transaction.annotation.Transactional;
import altn72.tpfilrouge.formulaire.ModificationIncident;
import altn72.tpfilrouge.exception.ModificationIncidentInterditeException;
import altn72.tpfilrouge.exception.ResolutionIncidentInterditeException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final EquipementService equipementService;
    private final EvaluationRepository evaluationRepository;
    private final Clock horloge;
    private final DroitsIncident droitsIncident;

    public IncidentService(IncidentRepository incidentRepository, EquipementService equipementService,
                           EvaluationRepository evaluationRepository, Clock horloge, DroitsIncident droitsIncident) {
        this.incidentRepository = incidentRepository;
        this.equipementService = equipementService;
        this.evaluationRepository = evaluationRepository;
        this.horloge = horloge;
        this.droitsIncident = droitsIncident;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESPONSABLE_EXPLOITATION', 'AGENT_SIGNALEMENT')")
    public Incident preparerModification(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", id));
        if (!droitsIncident.peutModifier(incident)) {
            throw new ModificationIncidentInterditeException();
        }
        return incident;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('RESPONSABLE_EXPLOITATION', 'AGENT_SIGNALEMENT')")
    public void modifierIncident(Integer id, ModificationIncident modification) {
        Incident incident = preparerModification(id);
        if (modification.getDescription() == null || modification.getDescription().isBlank()
                || modification.getDescription().length() > 255) {
            throw new IllegalArgumentException("La description est obligatoire et limitée à 255 caractères.");
        }
        if (!incident.getEquipement().getId().equals(modification.getEquipementId())) {
            if (!droitsIncident.peutChangerEquipement(incident)) {
                throw new AccessDeniedException("Vous ne pouvez pas changer l'équipement de cet incident.");
            }
            incident.setEquipement(equipementService.trouverEquipement(modification.getEquipementId()));
        }
        incident.setDescription(modification.getDescription().strip());
    }

    @Transactional(readOnly = true)
    public DetailIncident consulterDetail(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", id));
        return new DetailIncident(incident, calculerDureeOuverture(incident),
                evaluationRepository.findByIncident_Id(id).orElse(null));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('RESPONSABLE_EXPLOITATION', 'AGENT_SIGNALEMENT')")
    public List<Incident> consulterHistorique() {
        return incidentRepository
                .findByStatutAndDateHeureResolutionIsNotNullOrderByDateHeureResolutionDescIdDesc(Incident.STATUT_RESOLU);
    }

    @Transactional
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public void resoudreIncident(Integer id) {
        Incident incident = incidentRepository.trouverPourResolution(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", id));
        if (incident.getDateHeureResolution() != null || Incident.STATUT_RESOLU.equals(incident.getStatut())) {
            throw new ResolutionIncidentInterditeException("Cet incident est déjà résolu.");
        }
        LocalDateTime resolution = LocalDateTime.now(horloge);
        if (resolution.isBefore(incident.getDateHeureOuverture())) {
            throw new ResolutionIncidentInterditeException(
                    "La résolution ne peut pas être antérieure à l'ouverture de l'incident.");
        }
        incident.setStatut(Incident.STATUT_RESOLU);
        incident.setDateHeureResolution(resolution);
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
