package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.modele.UtilisateurRepository;

public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur getUtilisateurByUsername(String prenom, String profil) {
        return utilisateurRepository.findById(prenom + profil).orElse(null);
    }
}
