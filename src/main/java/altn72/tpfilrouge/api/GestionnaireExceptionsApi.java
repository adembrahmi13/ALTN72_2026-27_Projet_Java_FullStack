package altn72.tpfilrouge.api;

import altn72.tpfilrouge.exception.RessourceIntrouvableException;
import altn72.tpfilrouge.exception.ResolutionIncidentInterditeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice(assignableTypes = IncidentApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GestionnaireExceptionsApi extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException exception, HttpHeaders headers,
                                                        HttpStatusCode statut, WebRequest requete) {
        return new ResponseEntity<>(probleme(HttpStatus.BAD_REQUEST, "Requête invalide",
                "L'identifiant de l'incident doit être un entier."), headers, statut);
    }
    @ExceptionHandler(RessourceIntrouvableException.class)
    public ProblemDetail introuvable(RessourceIntrouvableException exception) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", exception.getMessage());
    }

    @ExceptionHandler(ResolutionIncidentInterditeException.class)
    public ProblemDetail conflit(ResolutionIncidentInterditeException exception) {
        return probleme(HttpStatus.CONFLICT, "Résolution impossible", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail interdit(AccessDeniedException exception) {
        return probleme(HttpStatus.FORBIDDEN, "Accès interdit", "Votre profil ne permet pas cette opération.");
    }

    private ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setTitle(titre);
        return probleme;
    }
}
