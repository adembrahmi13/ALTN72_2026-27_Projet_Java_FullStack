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
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import altn72.tpfilrouge.formulaire.ModificationIncident;
import altn72.tpfilrouge.service.DroitsIncident;
import altn72.tpfilrouge.repository.PrestataireRepository;

@Controller
public class IncidentController {
    private static final String VUE_NOUVEL_INCIDENT = "nouvelIncident";

    private final IncidentService incidentService;
    private final EquipementService equipementService;
    private final DroitsIncident droitsIncident;
    private final PrestataireRepository prestataireRepository;

    public IncidentController(IncidentService incidentService, EquipementService equipementService,
                              DroitsIncident droitsIncident, PrestataireRepository prestataireRepository) {
        this.incidentService = incidentService;
        this.equipementService = equipementService;
        this.droitsIncident = droitsIncident;
        this.prestataireRepository = prestataireRepository;
    }

    @InitBinder("modificationIncident")
    public void limiterChampsModification(WebDataBinder binder) {
        binder.setAllowedFields("description", "equipementId");
    }

    @GetMapping("/incidents/{id}/modifier")
    public String preparerModification(@PathVariable Integer id, Model model) {
        Incident incident = incidentService.preparerModification(id);
        ModificationIncident formulaire = new ModificationIncident();
        formulaire.setDescription(incident.getDescription());
        formulaire.setEquipementId(incident.getEquipement().getId());
        model.addAttribute("modificationIncident", formulaire);
        ajouterContexteModification(incident, model);
        return "modifierIncident";
    }

    @PostMapping("/incidents/{id}/modifier")
    public String modifierIncident(@PathVariable Integer id,
                                   @Valid @ModelAttribute("modificationIncident") ModificationIncident formulaire,
                                   BindingResult erreurs, Model model, RedirectAttributes redirection) {
        Incident incident = incidentService.preparerModification(id);
        if (erreurs.hasErrors()) {
            ajouterContexteModification(incident, model);
            return "modifierIncident";
        }
        incidentService.modifierIncident(id, formulaire);
        redirection.addFlashAttribute("messageSucces", "Les modifications ont été enregistrées.");
        return "redirect:/incidents/" + id;
    }

    private void ajouterContexteModification(Incident incident, Model model) {
        model.addAttribute("incident", incident);
        boolean autorise = droitsIncident.peutChangerEquipement(incident);
        model.addAttribute("changementEquipementAutorise", autorise);
        if (autorise) {
            model.addAttribute("equipements", equipementService.listerEquipements());
        }
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
        model.addAttribute("modificationAutorisee", droitsIncident.peutModifier(detail.incident()));
        model.addAttribute("resolutionAutorisee", droitsIncident.peutResoudre(detail.incident()));
        model.addAttribute("prestataires", prestataireRepository.findAll());
        return "detailIncident";
    }

    @GetMapping("/incidents/historique")
    public String consulterHistorique(Model model) {
        model.addAttribute("incidents", incidentService.consulterHistorique());
        return "historiqueIncidents";
    }

    @PostMapping("/incidents/{id}/resoudre")
    public String resoudreIncident(@PathVariable Integer id, RedirectAttributes redirection) {
        incidentService.resoudreIncident(id);
        redirection.addFlashAttribute("messageSucces", "L'incident a été déclaré résolu.");
        return "redirect:/incidents/" + id;
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
