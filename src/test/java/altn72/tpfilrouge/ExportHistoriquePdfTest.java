package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.IncidentRepository;
import altn72.tpfilrouge.service.ExportHistoriquePdfService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
class ExportHistoriquePdfTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired IncidentRepository incidents;
    @Autowired ExportHistoriquePdfService service;
    Equipement equipement;

    @BeforeEach
    void preparer() {
        Station station = stations.save(new Station("Station Étoile"));
        equipement = equipements.save(new Equipement("EQ-PDF", "Écran du hall", "Borne", "Opérationnel", station));
    }

    private void creer(String description, LocalDateTime resolution) {
        incidents.save(new Incident(equipement, description, LocalDateTime.of(2026, 10, 1, 9, 0),
                resolution == null ? Incident.STATUT_NON_AFFECTE : Incident.STATUT_RESOLU, null, resolution));
    }

    @Test
    void telechargementPdfLisibleTrieEtLimiteAuxResolus() throws Exception {
        creer("Ancien incident", LocalDateTime.of(2026, 10, 2, 12, 0));
        creer("Récent incident", LocalDateTime.of(2026, 10, 3, 12, 0));
        creer("Incident encore ouvert", null);
        byte[] contenu = mvc.perform(get("/incidents/historique/export.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("historique-incidents.pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        try (var document = Loader.loadPDF(contenu)) {
            String texte = new PDFTextStripper().getText(document);
            assertTrue(texte.contains("Station Étoile"));
            assertTrue(texte.contains("Écran du hall"));
            assertTrue(texte.contains("Sans prestataire"));
            assertTrue(texte.contains("03/10/2026 12:00"));
            assertTrue(texte.indexOf("Récent incident") < texte.indexOf("Ancien incident"));
            assertFalse(texte.contains("Incident encore ouvert"));
        }
    }

    @Test
    void historiqueVideProduitUnPdfValide() throws Exception {
        try (var document = Loader.loadPDF(service.exporter())) {
            assertEquals(1, document.getNumberOfPages());
            assertTrue(new PDFTextStripper().getText(document).contains("Aucun incident résolu"));
        }
    }

    @Test
    void exportMultipageConserveLongsTextesEtRemplaceCaracteresNonPrisEnCharge() throws Exception {
        for (int i = 0; i < 25; i++) {
            creer("Incident-" + i + " " + "é".repeat(160) + " 🚉\nSuite",
                    LocalDateTime.of(2026, 10, 2, 12, 0).plusHours(i));
        }
        try (var document = Loader.loadPDF(service.exporter())) {
            assertTrue(document.getNumberOfPages() > 1);
            String texte = new PDFTextStripper().getText(document);
            assertTrue(texte.contains("Incident-24"));
            assertTrue(texte.contains("Incident-0"));
            assertTrue(texte.contains("Suite"));
            assertTrue(texte.contains("Page 1 /"));
        }
    }

    @Test
    @WithUserDetails("agent")
    void exportInterditAgentMaisHistoriqueResteConsultable() throws Exception {
        assertThrows(AccessDeniedException.class, () -> service.exporter());
        mvc.perform(get("/incidents/historique/export.pdf")).andExpect(status().isForbidden());
        mvc.perform(get("/incidents/historique"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Exporter l&#39;historique en PDF"))))
                .andExpect(content().string(not(containsString("/export.pdf"))));
    }

    @Test
    void visiteurRedirigeEtResponsableVoitBouton() throws Exception {
        mvc.perform(get("/incidents/historique/export.pdf").with(anonymous()))
                .andExpect(redirectedUrl("/connexion"));
        mvc.perform(get("/incidents/historique"))
                .andExpect(content().string(containsString("/incidents/historique/export.pdf")));
    }
}
