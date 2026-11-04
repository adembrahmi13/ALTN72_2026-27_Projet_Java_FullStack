package altn72.tpfilrouge.repository;

// Résultat de la requête JPQL : Spring remplit ces getters à partir des alias "nom", "moyenne" et "nombreEvaluations"
public interface NoteMoyennePrestataire {
    String getNom();

    Double getMoyenne();

    Long getNombreEvaluations();
}
