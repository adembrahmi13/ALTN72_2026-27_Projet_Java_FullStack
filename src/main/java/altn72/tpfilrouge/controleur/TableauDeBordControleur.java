package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.modele.NiveauAttention;
import altn72.tpfilrouge.service.StationService;
import altn72.tpfilrouge.service.TableauDeBordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TableauDeBordControleur {

    private final TableauDeBordService tableauDeBordService;
    private final StationService stationService;

    public TableauDeBordControleur(TableauDeBordService tableauDeBordService, StationService stationService) {
        this.tableauDeBordService = tableauDeBordService;
        this.stationService = stationService;
    }

    // Les deux critères de filtre (station et niveau) arrivent dans l'URL, ex : /tableau-de-bord?niveau=ROUGE
    @GetMapping("/tableau-de-bord")
    public String afficherTableauDeBord(@RequestParam(required = false) Integer stationId,
                                        @RequestParam(required = false) NiveauAttention niveau,
                                        Model model) {
        model.addAttribute("incidents", tableauDeBordService.listerIncidentsActifs(stationId, niveau));
        model.addAttribute("compteurs", tableauDeBordService.compterParNiveau());
        model.addAttribute("incidentsParStation", tableauDeBordService.compterIncidentsActifsParStation());
        model.addAttribute("stations", stationService.listerStations());
        model.addAttribute("niveaux", NiveauAttention.values());
        model.addAttribute("stationChoisie", stationId);
        model.addAttribute("niveauChoisi", niveau);
        return "tableauDeBord";
    }
}
