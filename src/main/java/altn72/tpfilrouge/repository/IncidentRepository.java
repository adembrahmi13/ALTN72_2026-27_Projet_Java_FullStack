package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    // Sérialise deux demandes de clôture afin de conserver la première date de résolution.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Incident i where i.id = :id")
    Optional<Incident> trouverPourResolution(@Param("id") Integer id);

    // RG7 : les incidents d'un équipement sont ordonnés selon la date d'ouverture (le plus récent en premier)
    List<Incident> findByEquipement_IdOrderByDateHeureOuvertureDesc(Integer equipementId);
}
