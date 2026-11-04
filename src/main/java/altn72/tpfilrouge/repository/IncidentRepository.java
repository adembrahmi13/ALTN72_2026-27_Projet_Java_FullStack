package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    @EntityGraph(attributePaths = {"equipement", "equipement.station", "prestataire"})
    List<Incident> findAllByOrderByDateHeureOuvertureDescIdDesc();

    // Sérialise deux demandes de clôture afin de conserver la première date de résolution.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Incident i where i.id = :id")
    Optional<Incident> trouverPourResolution(@Param("id") Integer id);

    // RG7 : les incidents d'un équipement sont ordonnés selon la date d'ouverture (le plus récent en premier)
    List<Incident> findByEquipement_IdOrderByDateHeureOuvertureDesc(Integer equipementId);

    // RG6 : incidents actifs (non résolus) pour le tableau de bord, le plus ancien en premier
    @EntityGraph(attributePaths = {"equipement", "equipement.station", "prestataire"})
    List<Incident> findByDateHeureResolutionIsNullOrderByDateHeureOuvertureAscIdAsc();

    // Seule requête SQL native du projet (voir journal.md, section Persistance) :
    // nombre d'incidents actifs par station, pour les indicateurs du tableau de bord
    @Query(value = """
            SELECT s.nom AS nom, COUNT(i.id) AS nombre
            FROM incident i
            JOIN equipement e ON e.id = i.equipement_id
            JOIN station s ON s.id = e.station_id
            WHERE i.date_heure_resolution IS NULL
            GROUP BY s.id, s.nom
            ORDER BY nombre DESC, s.nom
            """, nativeQuery = true)
    List<IncidentsActifsParStation> compterIncidentsActifsParStation();

    // Lot alpha (visualisation) : nombre d'incidents par type d'équipement, le plus touché en premier
    @Query("""
            select e.type as type, count(i) as nombre
            from Incident i join i.equipement e
            group by e.type
            order by count(i) desc, e.type
            """)
    List<IncidentsParType> compterIncidentsParType();

    // RG7 : historique trié par clôture, puis identifiant pour un ordre stable à date égale.
    @EntityGraph(attributePaths = {"equipement", "equipement.station", "prestataire"})
    List<Incident> findByStatutAndDateHeureResolutionIsNotNullOrderByDateHeureResolutionDescIdDesc(String statut);
}
