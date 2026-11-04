package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.service.AffectationPrestataireService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AffectationPrestataireController {

    private final AffectationPrestataireService affectationService;

    public AffectationPrestataireController(
            AffectationPrestataireService affectationService) {
        this.affectationService = affectationService;
    }

    @PostMapping("/incidents/{incidentId}/affecter-prestataire")
    public String affecterPrestataire(@PathVariable Integer incidentId, @RequestParam Integer prestataireId) {
        affectationService.affecterPrestataire(incidentId, prestataireId);
        return "redirect:/incidents/" + incidentId;
    }

}