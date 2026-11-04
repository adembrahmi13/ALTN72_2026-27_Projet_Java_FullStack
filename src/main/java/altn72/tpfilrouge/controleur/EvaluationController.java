package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.formulaire.SaisieEvaluation;
import altn72.tpfilrouge.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EvaluationController {
    private static final String VUE_EVALUATION = "evaluerIncident";

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/incidents/{id}/evaluer")
    public String preparerEvaluation(@PathVariable Integer id, Model model) {
        model.addAttribute("incident", evaluationService.preparerEvaluation(id));
        model.addAttribute("saisieEvaluation", new SaisieEvaluation());
        return VUE_EVALUATION;
    }

    @PostMapping("/incidents/{id}/evaluer")
    public String evaluer(@PathVariable Integer id,
                          @Valid @ModelAttribute("saisieEvaluation") SaisieEvaluation saisie,
                          BindingResult erreurs, Model model, RedirectAttributes redirection) {
        if (erreurs.hasErrors()) {
            model.addAttribute("incident", evaluationService.preparerEvaluation(id));
            return VUE_EVALUATION;
        }
        evaluationService.evaluer(id, saisie);
        redirection.addFlashAttribute("messageSucces", "L'évaluation a été enregistrée.");
        return "redirect:/incidents/" + id;
    }
}
