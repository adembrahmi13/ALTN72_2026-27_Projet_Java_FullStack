package altn72.tpfilrouge.controleur;

import altn72.tpfilrouge.exception.ImportCsvException;
import altn72.tpfilrouge.service.ImportCsvService;
import altn72.tpfilrouge.service.ExportHistoriquePdfService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
public class EchangesDonneesController {
    private final ImportCsvService importCsv;
    private final ExportHistoriquePdfService exportPdf;

    public EchangesDonneesController(ImportCsvService importCsv, ExportHistoriquePdfService exportPdf) {
        this.importCsv = importCsv;
        this.exportPdf = exportPdf;
    }

    @GetMapping("/donnees/import")
    public String formulaireImport() {
        return "importDonnees";
    }

    @PostMapping("/donnees/import")
    public String importer(@RequestParam String cible, @RequestParam MultipartFile fichier,
                           Model model, RedirectAttributes redirection) {
        try {
            int nombre = importCsv.importer(cible, fichier);
            redirection.addFlashAttribute("messageSucces", nombre + " " + cible + " importé(s).");
            return "redirect:/donnees/import";
        } catch (ImportCsvException exception) {
            model.addAttribute("messageErreur", exception.getMessage() + " Aucune donnée n'a été importée.");
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("messageErreur", "Conflit avec les données existantes. Aucune donnée n'a été importée.");
        }
        model.addAttribute("cible", cible);
        return "importDonnees";
    }

    @GetMapping("/donnees/modeles/{cible}.csv")
    public ResponseEntity<byte[]> modele(@PathVariable String cible) {
        String exemple;
        if ("stations".equals(cible)) {
            exemple = "nom\r\nStation Centre\r\nStation Ouest\r\n";
        } else if ("equipements".equals(cible)) {
            exemple = "reference;libelle;type;station;etat\r\nEQ-CSV-001;Borne du hall;Borne;Station Centre;Opérationnel\r\n";
        } else {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cible + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + exemple).getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/incidents/historique/export.pdf")
    public ResponseEntity<byte[]> exporter() throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historique-incidents.pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_PDF)
                .body(exportPdf.exporter());
    }
}
