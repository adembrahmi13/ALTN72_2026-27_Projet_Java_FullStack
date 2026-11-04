package altn72.tpfilrouge;

import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.StationRepository;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.service.ImportCsvService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithUserDetails("responsable")
class ImportDonneesTest {
    @Autowired MockMvc mvc;
    @Autowired StationRepository stations;
    @Autowired EquipementRepository equipements;
    @Autowired ImportCsvService service;

    private MockMultipartFile csv(String texte) {
        return new MockMultipartFile("fichier", "donnees.csv", "text/csv", texte.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importStationsAvecBomAccentsEtGuillemets() throws Exception {
        mvc.perform(multipart("/donnees/import").file(csv("\uFEFFnom\r\n\"Station Étoile; Sud\"\r\nStation Ouest\r\n"))
                        .param("cible", "stations").with(csrf()))
                .andExpect(redirectedUrl("/donnees/import"))
                .andExpect(flash().attribute("messageSucces", "2 stations importé(s)."));
        assertEquals(1, stations.findByNomIgnoreCase("Station Étoile; Sud").size());
        assertEquals(1, stations.findByNomIgnoreCase("Station Ouest").size());
    }

    @Test
    void importEquipementsLieStationEtPreserveSeparateursEtRetoursLigne() throws Exception {
        Station station = stations.save(new Station("Station CSV"));
        mvc.perform(multipart("/donnees/import")
                        .file(csv("reference;libelle;type;station;etat\nEQ-CSV;\"Borne; côté\nquai\";Borne;station csv;Opérationnel\n"))
                        .param("cible", "equipements").with(csrf()))
                .andExpect(status().is3xxRedirection());
        var equipement = equipements.findByStation_Id(station.getId()).getFirst();
        assertEquals("Borne; côté\nquai", equipement.getLibelleDescription());
        assertEquals("EQ-CSV", equipement.getReferenceUnique());
    }

    @Test
    void doublonDansFichierAnnuleTout() throws Exception {
        long avant = stations.count();
        mvc.perform(multipart("/donnees/import").file(csv("nom\nStation Nouvelle\nstation nouvelle\n"))
                        .param("cible", "stations").with(csrf()))
                .andExpect(view().name("importDonnees"))
                .andExpect(model().attribute("messageErreur", containsString("Ligne de données 2")));
        assertEquals(avant, stations.count());
    }

    @Test
    void stationDejaExistanteRefuseeSansCreationPartielle() throws Exception {
        stations.save(new Station("Station Existante"));
        mvc.perform(multipart("/donnees/import").file(csv("nom\nStation Nouvelle\nstation existante\n"))
                        .param("cible", "stations").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("existe déjà")));
        assertTrue(stations.findByNomIgnoreCase("Station Nouvelle").isEmpty());
    }

    @Test
    void referenceExistanteOuRepeteeRefusee() throws Exception {
        stations.save(new Station("Station CSV"));
        String entete = "reference;libelle;type;station;etat\n";
        String ligne = "EQ-CSV;Borne;Borne;Station CSV;Opérationnel\n";
        mvc.perform(multipart("/donnees/import").file(csv(entete + ligne + ligne.toLowerCase(java.util.Locale.ROOT)))
                        .param("cible", "equipements").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("existe déjà")));
        assertFalse(equipements.existsByReferenceUniqueIgnoreCase("EQ-CSV"));
        mvc.perform(multipart("/donnees/import").file(csv(entete + ligne))
                        .param("cible", "equipements").with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/donnees/import").file(csv(entete + ligne))
                        .param("cible", "equipements").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("existe déjà")));
    }

    @Test
    void stationInconnueOuAmbigueRefusee() throws Exception {
        String texte = "reference;libelle;type;station;etat\nEQ-CSV;Borne;Borne;Station CSV;Opérationnel\n";
        mvc.perform(multipart("/donnees/import").file(csv(texte)).param("cible", "equipements").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("inconnue ou ambiguë")));
        stations.save(new Station("Station CSV"));
        stations.save(new Station("Station CSV"));
        mvc.perform(multipart("/donnees/import").file(csv(texte)).param("cible", "equipements").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("inconnue ou ambiguë")));
        assertFalse(equipements.existsByReferenceUniqueIgnoreCase("EQ-CSV"));
    }

    @Test
    void formatsInvalidesRefusesAvecMessageEtSansEcriture() throws Exception {
        long avant = stations.count();
        for (String texte : new String[]{"", "nom\n", "mauvais\nStation\n", "nom\nStation;Autre\n",
                "nom\n\"" + "x".repeat(101) + "\"\n", "nom\n\"guillemet non fermé"}) {
            mvc.perform(multipart("/donnees/import").file(csv(texte)).param("cible", "stations").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("messageErreur"));
        }
        assertEquals(avant, stations.count());
    }

    @Test
    void mauvaisEncodageTailleEtNombreLignesRefuses() throws Exception {
        var invalide = new MockMultipartFile("fichier", "test.csv", "text/csv",
                new byte[]{'n', 'o', 'm', '\n', (byte) 0xC3, (byte) 0x28});
        mvc.perform(multipart("/donnees/import").file(invalide).param("cible", "stations").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("UTF-8")));
        mvc.perform(multipart("/donnees/import")
                        .file(new MockMultipartFile("fichier", new byte[ImportCsvService.TAILLE_MAX + 1]))
                        .param("cible", "stations").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("1 Mio")));
        mvc.perform(multipart("/donnees/import").file(csv("nom\n" + "Station\n".repeat(5001)))
                        .param("cible", "stations").with(csrf()))
                .andExpect(model().attribute("messageErreur", containsString("5 000")));
    }

    @Test
    void pageEtModelesAccessiblesAuResponsable() throws Exception {
        mvc.perform(get("/donnees/import")).andExpect(status().isOk()).andExpect(view().name("importDonnees"));
        mvc.perform(get("/donnees/modeles/equipements.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("equipements.csv")))
                .andExpect(content().string(containsString("reference;libelle;type;station;etat")));
        mvc.perform(get("/donnees/modeles/stations.csv"))
                .andExpect(content().string(containsString("Station Centre")));
    }

    @Test
    @WithUserDetails("agent")
    void agentRefuseParServiceEtRoutes() throws Exception {
        assertThrows(AccessDeniedException.class, () -> service.importer("stations", csv("nom\nStation\n")));
        mvc.perform(get("/donnees/import")).andExpect(status().isForbidden());
        mvc.perform(get("/donnees/modeles/stations.csv")).andExpect(status().isForbidden());
        mvc.perform(multipart("/donnees/import").file(csv("nom\nStation\n"))
                        .param("cible", "stations").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/stations")).andExpect(content().string(not(containsString("Importer des données"))));
    }

    @Test
    void visiteurEtAbsenceCsrfRefuses() throws Exception {
        mvc.perform(get("/donnees/import").with(anonymous())).andExpect(redirectedUrl("/connexion"));
        mvc.perform(multipart("/donnees/import").file(csv("nom\nStation\n")).param("cible", "stations"))
                .andExpect(status().isForbidden());
    }
}
