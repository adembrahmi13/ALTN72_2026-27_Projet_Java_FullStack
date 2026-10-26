package altn72.tpfilrouge.modele;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
public class Utilisateur {

    @Id
    @Column(name = "identifiant_connexion", length = 50)
    private String login;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasseHache;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Profil profil;

    protected Utilisateur() {
    }

    public Utilisateur(String login, String motDePasseHache, String prenom, Profil profil) {
        this.login = login;
        this.motDePasseHache = motDePasseHache;
        this.prenom = prenom;
        this.profil = profil;
    }

    public String getLogin() {
        return login;
    }

    public @Nullable String getMotDePasseHache() {
        return motDePasseHache;
    }

    public String getPrenom() {
        return prenom;
    }

    public Profil getProfil() {
        return profil;
    }
}