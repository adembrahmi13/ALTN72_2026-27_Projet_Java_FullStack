package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.EvaluationInterditeException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.formulaire.SaisieEvaluation;
import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.repository.EvaluationRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class EvaluationService {
    private static final int NOTE_MIN = 1;
    private static final int NOTE_MAX = 5;

    private final IncidentRepository incidentRepository;
    private final EvaluationRepository evaluationRepository;
    private final Clock horloge;

    public EvaluationService(IncidentRepository incidentRepository, EvaluationRepository evaluationRepository, Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.evaluationRepository = evaluationRepository;
        this.horloge = horloge;
    }

    // RG8 : seul un incident résolu, affecté à un prestataire et pas encore évalué peut être évalué
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public Incident preparerEvaluation(Integer incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", incidentId));
        if (incident.getDateHeureResolution() == null) {
            throw new EvaluationInterditeException("Seul un incident résolu peut être évalué.");
        }
        if (incident.getPrestataireId() == null) {
            throw new EvaluationInterditeException("Cet incident n'a été affecté à aucun prestataire.");
        }
        if (evaluationRepository.existsByIncident_Id(incidentId)) {
            throw new EvaluationInterditeException("Cet incident a déjà été évalué.");
        }
        return incident;
    }

    @Transactional
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public void evaluer(Integer incidentId, SaisieEvaluation saisie) {
        Incident incident = preparerEvaluation(incidentId);
        Integer note = saisie.getNote();
        if (note == null || note < NOTE_MIN || note > NOTE_MAX) {
            throw new EvaluationInterditeException("La note doit être un entier compris entre 1 et 5.");
        }
        evaluationRepository.save(new Evaluation(incident, note, saisie.getCommentaire(), LocalDate.now(horloge)));
    }
}
