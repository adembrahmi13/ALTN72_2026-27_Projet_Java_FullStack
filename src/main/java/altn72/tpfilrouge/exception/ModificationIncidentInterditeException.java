package altn72.tpfilrouge.exception;

public class ModificationIncidentInterditeException extends RuntimeException {
    public ModificationIncidentInterditeException() {
        super("Un incident résolu ne peut plus être modifié.");
    }
}
