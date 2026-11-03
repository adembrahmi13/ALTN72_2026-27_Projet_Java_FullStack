package altn72.tpfilrouge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GestionnaireExceptionsWeb {

    private static final String VUE_ERREUR = "erreur";

    @ExceptionHandler(ModificationIncidentInterditeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String gererModificationInterdite(ModificationIncidentInterditeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String gererAccesInterdit(org.springframework.security.access.AccessDeniedException exception, Model model) {
        model.addAttribute("message", "Votre profil ne permet pas cette modification.");
        return VUE_ERREUR;
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String gererRessourceIntrouvable(RessourceIntrouvableException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }
}
