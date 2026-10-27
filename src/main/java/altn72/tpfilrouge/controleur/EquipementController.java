package altn72.tpfilrouge.controleur;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.modele.Equipement;
import java.util.List;


@Controller 
public class EquipementController {
    private final EquipementService equipementService;

    public EquipementController(EquipementService equipementService) {
        this.equipementService = equipementService;
    }

    @GetMapping("/stations/{stationId}/equipements")
    public String getEquipementsByStationId(@PathVariable Integer stationId, Model model) {
        List<Equipement> equipements = equipementService.getEquipementsByStationId(stationId);
        model.addAttribute("equipements", equipements);
        return "equipements";
    }

}