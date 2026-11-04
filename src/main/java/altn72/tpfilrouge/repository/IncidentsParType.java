package altn72.tpfilrouge.repository;

// Résultat de la requête JPQL : Spring remplit ces getters à partir des alias "type" et "nombre"
public interface IncidentsParType {
    String getType();

    Long getNombre();
}
