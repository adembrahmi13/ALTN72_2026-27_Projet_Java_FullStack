package altn72.tpfilrouge.service;

import org.springframework.stereotype.Service;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.transaction.annotation.Transactional;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;

// Fichier pour gérér les affectations aux prestataires

@Service
public class AffectationPrestataireService {

    private final IncidentRepository incidentRepository;
    private final PrestataireRepository prestataireRepository;

    public AffectationPrestataireService(IncidentRepository incidentRepository, PrestataireRepository prestataireRepository) {
        this.incidentRepository = incidentRepository;
        this.prestataireRepository = prestataireRepository;
    }

    @Transactional
    public void affecterPrestataire(Integer incidentId, Integer prestataireId) {

        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new IllegalArgumentException("Incident introuvable : " + incidentId));

        Prestataire prestataire = prestataireRepository.findById(prestataireId)
            .orElseThrow(() -> new IllegalArgumentException("Prestataire introuvable : " + prestataireId));

        if (incident.getDateHeureResolution() != null || "Résolu".equalsIgnoreCase(incident.getStatut())) {
            throw new IllegalStateException(
                "Impossible d'affecter un prestataire à un incident résolu."
            );
        }

        incident.setPrestataireId(prestataire.getId());
        incident.setStatut(Incident.STATUT_AFFECTE);
        incidentRepository.save(incident);
    }

}
