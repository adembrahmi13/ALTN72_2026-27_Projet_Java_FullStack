package altn72.tpfilrouge.controleur;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConnexionControleur {

    @GetMapping("/connexion")
    public String afficherPageConnexion() {
        return "connexion";
    }
}