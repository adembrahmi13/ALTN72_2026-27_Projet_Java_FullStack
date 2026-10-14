package altn72.tpfilrouge.modele;
import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identifiant_connexion", nullable = false)
    private String identifiant_connexion;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "profil", nullable = false, length = 100)
    private String profil;

    public String getIdentifiantConnexion() {
        return identifiant_connexion;
    }

    public void setIdentifiant_connexion(String identifiant_connexion) {
        this.identifiant_connexion = identifiant_connexion;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

}