package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "equipement")
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column (name = "reference_unique", nullable = false, unique = true) // Ajout du au _ pour dire a JPA que l'attribut referenceUnique est unique et ne peut pas être null
    private String referenceUnique;
    @Column (name = "libelle_description", columnDefinition = "TEXT") // Ajout du au _ pour dire a JPA que l'attribut libelleDescription est de type TEXT dans la base de données
    private String libelleDescription;
    private String type;
    @Column (name = "etat_fonctionnement", nullable = false) // Ajout du au _ pour dire a JPA que l'attribut etatFonctionnement ne peut pas être null 
    private String etatFonctionnement; 
    @ManyToOne 
    @JoinColumn(name = "station_id", nullable = false) // Ajout du au _ pour dire a JPA que l'attribut station_id ne peut pas être null
    private Station station;

    public Equipement() {
    }

    public Equipement(String referenceUnique, String libelleDescription, String type, String etatFonctionnement, Station station) {
        this.referenceUnique = referenceUnique;
        this.libelleDescription = libelleDescription;
        this.type = type;
        this.etatFonctionnement = etatFonctionnement;
        this.station = station;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReferenceUnique() {
        return referenceUnique;
    }

    public void setReferenceUnique(String referenceUnique) {
        this.referenceUnique = referenceUnique;
    }

    public String getLibelleDescription() {
        return libelleDescription;
    }

    public void setLibelleDescription(String libelleDescription) {
        this.libelleDescription = libelleDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEtatFonctionnement() {
        return etatFonctionnement;
    }

    public void setEtatFonctionnement(String etatFonctionnement) {
        this.etatFonctionnement = etatFonctionnement;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}