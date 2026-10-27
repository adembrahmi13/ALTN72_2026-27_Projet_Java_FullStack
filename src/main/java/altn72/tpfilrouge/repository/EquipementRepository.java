package altn72.tpfilrouge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import altn72.tpfilrouge.modele.Equipement;
import java.util.List;

// Spring Data JPA va automatiquement générer l'implémentation de cette interface pour nous 
// nous n'avons donc pas besoin d'écrire de code supplémentaire pour effectuer des opérations CRUD sur les entités Equipement.
public interface EquipementRepository extends JpaRepository<Equipement, Integer> {

    List<Equipement> findByStation_Id(Integer stationId);
    
}