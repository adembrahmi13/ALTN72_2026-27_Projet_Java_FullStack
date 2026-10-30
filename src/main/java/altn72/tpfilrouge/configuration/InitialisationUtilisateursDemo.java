package altn72.tpfilrouge.configuration;

import altn72.tpfilrouge.modele.Profil;
import altn72.tpfilrouge.modele.Utilisateur;
import altn72.tpfilrouge.repository.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialisationUtilisateursDemo implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder encodeurMotDePasse;
    private final String motDePasseDemo;

    public InitialisationUtilisateursDemo(UtilisateurRepository utilisateurRepository,
                                          PasswordEncoder encodeurMotDePasse,
                                          @Value("${demo.mot-de-passe}") String motDePasseDemo) {
        this.utilisateurRepository = utilisateurRepository;
        this.encodeurMotDePasse = encodeurMotDePasse;
        this.motDePasseDemo = motDePasseDemo;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        creerSiAbsent("responsable", "Camille", Profil.RESPONSABLE_EXPLOITATION);
        creerSiAbsent("agent", "Alex", Profil.AGENT_SIGNALEMENT);
    }

    private void creerSiAbsent(String login, String prenom, Profil profil) {
        if (utilisateurRepository.findByLogin(login).isEmpty()) {
            String motDePasseHache = encodeurMotDePasse.encode(motDePasseDemo);
            utilisateurRepository.save(new Utilisateur(login, motDePasseHache, prenom, profil));
        }
    }
}