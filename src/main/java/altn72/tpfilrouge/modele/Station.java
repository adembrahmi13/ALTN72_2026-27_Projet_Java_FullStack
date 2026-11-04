package altn72.tpfilrouge.modele;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity 
@Table(name = "station")
public class Station {
    // Pour dire a JPA que l'attribut id est la clé primaire et qu'il est auto-incrémenté 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 
    private String nom; 

    // Constructeur vide obligatoire pour JPA
    public Station() {
    }

    // Constructeur initialisant nom
    // Pas besoin d'initialiser id car il est auto-incrémenté grâce a @GeneratedValue
    public Station( String nom) {
        this.nom = nom;
    }

    // Getters 
    public Integer getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    // Setters 
    public void setId(Integer id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    
}
