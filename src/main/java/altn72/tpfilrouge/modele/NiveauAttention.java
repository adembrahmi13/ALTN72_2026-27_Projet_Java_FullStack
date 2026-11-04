package altn72.tpfilrouge.modele;

// Niveau d'attention d'un incident actif affiché sur le tableau de bord (RG3 et RG4)
// Il n'est jamais enregistré en base : il est recalculé à chaque affichage
public enum NiveauAttention {
    VERT("Vert"),
    ORANGE("Orange"),
    ROUGE("Rouge"),
    AFFECTE("Pris en charge");

    private final String libelle;

    NiveauAttention(String libelle) {
        this.libelle = libelle;
    }

    // Getter
    public String getLibelle() {
        return libelle;
    }
}
