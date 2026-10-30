package altn72.tpfilrouge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GestionnaireExceptionsWeb {

    private static final String VUE_ERREUR = "erreur";

    @ExceptionHandler(RessourceIntrouvableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String gererRessourceIntrouvable(RessourceIntrouvableException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }
}
