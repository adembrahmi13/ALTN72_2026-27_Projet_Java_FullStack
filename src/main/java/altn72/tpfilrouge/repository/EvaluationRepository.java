package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    Optional<Evaluation> findByIncident_Id(Integer incidentId);

    boolean existsByIncident_Id(Integer incidentId);

    // Lot alpha (visualisation) : moyenne des notes de chaque prestataire, la meilleure en premier
    @Query("""
            select p.nom as nom, avg(e.note) as moyenne, count(e) as nombreEvaluations
            from Evaluation e join e.incident i join i.prestataire p
            group by p.id, p.nom
            order by avg(e.note) desc, p.nom
            """)
    List<NoteMoyennePrestataire> calculerNoteMoyenneParPrestataire();
}
