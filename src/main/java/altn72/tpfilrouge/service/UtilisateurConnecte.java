package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class UtilisateurConnecte extends User {

    private static final String PREFIXE_ROLE = "ROLE_";

    private final String prenom;
    private final Profil profil;

    public UtilisateurConnecte(Utilisateur utilisateur) {
        super(utilisateur.getLogin(),
                utilisateur.getMotDePasseHache(),
                List.of(new SimpleGrantedAuthority(PREFIXE_ROLE + utilisateur.getProfil().name())));
        this.prenom = utilisateur.getPrenom();
        this.profil = utilisateur.getProfil();
    }

    public String getPrenom() {
        return prenom;
    }

    public Profil getProfil() {
        return profil;
    }
}