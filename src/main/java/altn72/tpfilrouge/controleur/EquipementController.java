package altn72.tpfilrouge.controleur;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import altn72.tpfilrouge.service.EquipementService;
import altn72.tpfilrouge.service.StationService;


@Controller
public class EquipementController {
    private final EquipementService equipementService;
    private final StationService stationService;

    public EquipementController(EquipementService equipementService, StationService stationService) {
        this.equipementService = equipementService;
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    public String afficherStations(Model model) {
        model.addAttribute("stations", stationService.listerStations());
        return "stations";
    }

    @GetMapping("/stations/{stationId}/equipements")
    public String getEquipementsByStationId(@PathVariable Integer stationId, Model model) {
        model.addAttribute("station", stationService.trouverStation(stationId));
        model.addAttribute("equipements", equipementService.getEquipementsByStationId(stationId));
        return "equipements";
    }

    @GetMapping("/equipements/{equipementId}")
    public String afficherFicheEquipement(@PathVariable Integer equipementId, Model model) {
        model.addAttribute("equipement", equipementService.trouverEquipement(equipementId));
        model.addAttribute("incidents", equipementService.listerIncidents(equipementId));
        return "ficheEquipement";
    }

}
