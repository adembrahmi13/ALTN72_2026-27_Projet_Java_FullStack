package altn72.tpfilrouge.exception;

public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String typeRessource, Integer id) {
        super(typeRessource + " introuvable (identifiant " + id + ").");
    }
}
