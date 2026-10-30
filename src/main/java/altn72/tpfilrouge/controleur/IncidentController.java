package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class IncidentController {
    private static final String VUE_NOUVEL_INCIDENT = "nouvelIncident";

    private final IncidentService incidentService;
    private final EquipementService equipementService;

    public IncidentController(IncidentService incidentService, EquipementService equipementService) {
        this.incidentService = incidentService;
        this.equipementService = equipementService;
    }

    @GetMapping("/equipements/{equipementId}/incidents/nouveau")
    public String preparerDeclarationIncident(@PathVariable Integer equipementId, Model model) {
        model.addAttribute("equipement", equipementService.trouverEquipement(equipementId));
        model.addAttribute("nouvelIncident", new Incident());
        return VUE_NOUVEL_INCIDENT;
    }

    @GetMapping("/incidents/{id}")
    public String consulterIncident(@PathVariable Integer id, Model model) {
        var detail = incidentService.consulterDetail(id);
        model.addAttribute("detail", detail);
        model.addAttribute("incident", detail.incident());
        return "detailIncident";
    }

    @PostMapping("/equipements/{equipementId}/incidents")
    public String declarerIncident(@PathVariable Integer equipementId,
                                   @Valid @ModelAttribute("nouvelIncident") Incident nouvelIncident,
                                   BindingResult erreurs, Model model) {
        if (erreurs.hasErrors()) {
            model.addAttribute("equipement", equipementService.trouverEquipement(equipementId));
            return VUE_NOUVEL_INCIDENT;
        }
        incidentService.declarerIncident(equipementId, nouvelIncident.getDescription());
        return "redirect:/equipements/" + equipementId;
    }
}
