package altn72.tpfilrouge.api;

import java.time.LocalDateTime;
import altn72.tpfilrouge.modele.Incident;

public record ResumeIncidentApi(Integer id, String description, String statut,
                                LocalDateTime dateHeureOuverture, LocalDateTime dateHeureResolution,
                                EquipementApi equipement, PrestataireApi prestataire) {
    public record StationApi(Integer id, String nom) {}
    public record EquipementApi(Integer id, String reference, String libelle, String type, StationApi station) {}
    public record PrestataireApi(Integer id, String nom, String contact) {}

    public static ResumeIncidentApi depuis(Incident incident) {
        var equipement = incident.getEquipement();
        var station = equipement.getStation();
        var prestataire = incident.getPrestataire();
        return new ResumeIncidentApi(incident.getId(), incident.getDescription(), incident.getStatut(),
                incident.getDateHeureOuverture(), incident.getDateHeureResolution(),
                new EquipementApi(equipement.getId(), equipement.getReferenceUnique(),
                        equipement.getLibelleDescription(), equipement.getType(), new StationApi(station.getId(), station.getNom())),
                prestataire == null ? null : new PrestataireApi(prestataire.getId(), prestataire.getNom(), prestataire.getContact()));
    }
}
