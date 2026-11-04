package altn72.tpfilrouge.service;

import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.IncidentsParType;
import altn72.tpfilrouge.repository.NoteMoyennePrestataire;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lot alpha, piste 2 : données des graphiques de la page Statistiques
@Service
public class StatistiquesService {

    private final EvaluationRepository evaluationRepository;
    private final IncidentRepository incidentRepository;

    public StatistiquesService(EvaluationRepository evaluationRepository, IncidentRepository incidentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.incidentRepository = incidentRepository;
    }

    // Note moyenne de chaque prestataire évalué, de la meilleure à la moins bonne
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public List<NoteMoyennePrestataire> listerNotesMoyennes() {
        return evaluationRepository.calculerNoteMoyenneParPrestataire();
    }

    // Nombre d'incidents (ouverts et résolus) par type d'équipement, du plus touché au moins touché
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public List<IncidentsParType> listerIncidentsParType() {
        return incidentRepository.compterIncidentsParType();
    }
}
