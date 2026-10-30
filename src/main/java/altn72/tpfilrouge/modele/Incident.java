package altn72.tpfilrouge.modele;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
public class Incident {
    public static final String STATUT_NON_AFFECTE = "Non affecté";

    // Pour dire a JPA que l'attribut id est la clé primaire et qu'il est auto-incrémenté
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "equipement_id", nullable = false)
    private Equipement equipement;
    @NotBlank(message = "La description de l'incident est obligatoire.")
    private String description;
    @Column(name = "date_heure_ouverture", nullable = false)
    private LocalDateTime dateHeureOuverture;
    private String statut;
    @Column(name = "prestataire_id")
    private Integer prestataireId;
    @Column(name = "date_heure_resolution")
    private LocalDateTime dateHeureResolution;

    // Constructeur vide obligatoire pour JPA
    public Incident() {
    }

    // Constructeur initialisant tous les attributs sauf id (auto-incrémenté grâce a @GeneratedValue)
    public Incident(Equipement equipement, String description, LocalDateTime dateHeureOuverture, String statut, Integer prestataireId, LocalDateTime dateHeureResolution) {
        this.equipement = equipement;
        this.description = description;
        this.dateHeureOuverture = dateHeureOuverture;
        this.statut = statut;
        this.prestataireId = prestataireId;
        this.dateHeureResolution = dateHeureResolution;
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public Equipement getEquipement() {
        return equipement;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateHeureOuverture() {
        return dateHeureOuverture;
    }

    public String getStatut() {
        return statut;
    }

    public Integer getPrestataireId() {
        return prestataireId;
    }

    public LocalDateTime getDateHeureResolution() {
        return dateHeureResolution;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateHeureOuverture(LocalDateTime dateHeureOuverture) {
        this.dateHeureOuverture = dateHeureOuverture;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setPrestataireId(Integer prestataireId) {
        this.prestataireId = prestataireId;
    }

    public void setDateHeureResolution(LocalDateTime dateHeureResolution) {
        this.dateHeureResolution = dateHeureResolution;
    }

}
