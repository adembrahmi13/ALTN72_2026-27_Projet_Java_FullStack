package altn72.tpfilrouge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GestionnaireExceptionsWeb {

    private static final String VUE_ERREUR = "erreur";

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String gererFichierTropVolumineux(Model model) {
        model.addAttribute("message", "Le fichier dépasse la taille autorisée. Importez un CSV de 1 Mio maximum.");
        return VUE_ERREUR;
    }

    // Paramètre d'URL invalide (ex : /tableau-de-bord?niveau=VIOLET) : message clair au lieu de la page technique de Spring
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String gererParametreInvalide(Model model) {
        model.addAttribute("message", "Un paramètre de l'adresse est invalide.");
        return VUE_ERREUR;
    }

    @ExceptionHandler(ResolutionIncidentInterditeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String gererResolutionInterdite(ResolutionIncidentInterditeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }

    @ExceptionHandler(AffectationIncidentInterditeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String gererAffectationInterdite(AffectationIncidentInterditeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }

    @ExceptionHandler(EvaluationInterditeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String gererEvaluationInterdite(EvaluationInterditeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return VUE_ERREUR;
    }

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
