package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    // RG7 : les incidents d'un équipement sont ordonnés selon la date d'ouverture (le plus récent en premier)
    List<Incident> findByEquipement_IdOrderByDateHeureOuvertureDesc(Integer equipementId);
}
