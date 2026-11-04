package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.AffectationIncidentInterditeException;
import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Prestataire;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.PrestataireRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AffectationPrestataireService {

    private final IncidentRepository incidentRepository;
    private final PrestataireRepository prestataireRepository;

    public AffectationPrestataireService(IncidentRepository incidentRepository, PrestataireRepository prestataireRepository) {
        this.incidentRepository = incidentRepository;
        this.prestataireRepository = prestataireRepository;
    }

    public List<Prestataire> listerPrestataires() {
        return prestataireRepository.findAll();
    }

    // RG5 : un incident résolu ne peut plus être affecté ni réaffecté
    @Transactional
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public void affecterPrestataire(Integer incidentId, Integer prestataireId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RessourceIntrouvableException("Incident", incidentId));
        Prestataire prestataire = prestataireRepository.findById(prestataireId)
                .orElseThrow(() -> new RessourceIntrouvableException("Prestataire", prestataireId));
        if (incident.getDateHeureResolution() != null || Incident.STATUT_RESOLU.equals(incident.getStatut())) {
            throw new AffectationIncidentInterditeException("Impossible d'affecter un prestataire à un incident résolu.");
        }
        incident.setPrestataireId(prestataire.getId());
        incident.setStatut(Incident.STATUT_AFFECTE);
    }
}
