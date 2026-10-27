package altn72.tpfilrouge.service;

import org.springframework.stereotype.Service;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.modele.Equipement;
import java.util.List;

// La classe EquipementService est annotée avec @Service pour indiquer à Spring qu'il s'agit d'un service métier.
@Service 
public class EquipementService {
    private final EquipementRepository equipementRepository;

    public EquipementService(EquipementRepository equipementRepository) {
        this.equipementRepository = equipementRepository;
    }

    public List<Equipement> getEquipementsByStationId(Integer stationId) {
        return equipementRepository.findByStation_Id(stationId);
    }
    
}
