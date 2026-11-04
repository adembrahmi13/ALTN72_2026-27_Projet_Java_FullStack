package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "prestataire")
public class Prestataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String contact;

    public Prestataire() {}

    public Prestataire(String nom, String contact) {
        this.nom = nom;
        this.contact = contact;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public String getContact() { return contact; }

    public void setId(Integer id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setContact(String contact) { this.contact = contact; }
}

