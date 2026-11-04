package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.NiveauAttention;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.repository.IncidentsActifsParStation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TableauDeBordService {

    // RG3 : les seuils de l'indicateur, définis à un seul endroit
    public static final long SEUIL_ORANGE_HEURES = 48;
    public static final long SEUIL_ROUGE_HEURES = 72;

    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final Clock horloge;

    public TableauDeBordService(IncidentRepository incidentRepository, IncidentService incidentService, Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
        this.horloge = horloge;
    }

    // RG3 et RG4 : calcule le niveau à partir de l'heure courante
    public NiveauAttention calculerNiveau(Incident incident) {
        // RG4 : un incident affecté n'a jamais de couleur vert / orange / rouge
        if (incident.getPrestataireId() != null) {
            return NiveauAttention.AFFECTE;
        }
        // toHours() arrondit vers le bas : 47 h 59 donne 47, donc vert
        long heuresOuvert = Duration.between(incident.getDateHeureOuverture(), LocalDateTime.now(horloge)).toHours();
        if (heuresOuvert >= SEUIL_ROUGE_HEURES) {
            return NiveauAttention.ROUGE;
        }
        if (heuresOuvert >= SEUIL_ORANGE_HEURES) {
            return NiveauAttention.ORANGE;
        }
        return NiveauAttention.VERT;
    }

    // RG6 : seuls les incidents non résolus sont affichés, du plus ancien au plus récent
    // Les deux filtres sont facultatifs : null veut dire "tous"
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public List<IncidentActif> listerIncidentsActifs(Integer stationId, NiveauAttention niveau) {
        List<IncidentActif> incidentsActifs = new ArrayList<>();
        for (Incident incident : incidentRepository.findByDateHeureResolutionIsNullOrderByDateHeureOuvertureAscIdAsc()) {
            NiveauAttention niveauIncident = calculerNiveau(incident);
            boolean bonneStation = stationId == null || incident.getEquipement().getStation().getId().equals(stationId);
            boolean bonNiveau = niveau == null || niveauIncident == niveau;
            if (bonneStation && bonNiveau) {
                incidentsActifs.add(new IncidentActif(incident, niveauIncident, incidentService.calculerDureeOuverture(incident)));
            }
        }
        return incidentsActifs;
    }

    // Indicateurs synthétiques : nombre d'incidents actifs pour chaque niveau
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public Map<NiveauAttention, Integer> compterParNiveau() {
        Map<NiveauAttention, Integer> compteurs = new EnumMap<>(NiveauAttention.class);
        for (NiveauAttention niveau : NiveauAttention.values()) {
            compteurs.put(niveau, 0);
        }
        for (IncidentActif incidentActif : listerIncidentsActifs(null, null)) {
            compteurs.put(incidentActif.niveau(), compteurs.get(incidentActif.niveau()) + 1);
        }
        return compteurs;
    }

    // Indicateur synthétique : nombre d'incidents actifs par station (requête SQL native)
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public List<IncidentsActifsParStation> compterIncidentsActifsParStation() {
        return incidentRepository.compterIncidentsActifsParStation();
    }
}
