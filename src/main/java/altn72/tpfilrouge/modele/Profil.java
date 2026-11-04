package altn72.tpfilrouge.modele;

public enum Profil {
    RESPONSABLE_EXPLOITATION("Responsable d'exploitation"),
    AGENT_SIGNALEMENT("Agent de signalement");

    private final String libelle;

    Profil(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}