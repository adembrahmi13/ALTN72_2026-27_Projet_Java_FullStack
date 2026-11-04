package altn72.tpfilrouge.service;

import altn72.tpfilrouge.exception.ImportCsvException;
import altn72.tpfilrouge.modele.Equipement;
import altn72.tpfilrouge.modele.Station;
import altn72.tpfilrouge.repository.EquipementRepository;
import altn72.tpfilrouge.repository.StationRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@Service
@PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
public class ImportCsvService {
    public static final int TAILLE_MAX = 1_048_576;
    public static final int LIGNES_MAX = 5_000;
    public static final List<String> COLONNES_STATIONS = List.of("nom");
    public static final List<String> COLONNES_EQUIPEMENTS =
            List.of("reference", "libelle", "type", "station", "etat");

    private final StationRepository stations;
    private final EquipementRepository equipements;

    public ImportCsvService(StationRepository stations, EquipementRepository equipements) {
        this.stations = stations;
        this.equipements = equipements;
    }

    @Transactional
    public int importer(String cible, MultipartFile fichier) {
        if (!"stations".equals(cible) && !"equipements".equals(cible)) {
            throw new ImportCsvException("Choisissez stations ou équipements.");
        }
        List<CSVRecord> lignes = lire(fichier, "stations".equals(cible) ? COLONNES_STATIONS : COLONNES_EQUIPEMENTS);
        var doublons = new HashSet<String>();
        var nouvellesStations = new ArrayList<Station>();
        var nouveauxEquipements = new ArrayList<Equipement>();
        for (CSVRecord ligne : lignes) {
            if ("stations".equals(cible)) {
                String nom = valeur(ligne, "nom", 100);
                if (!doublons.add(nom.toLowerCase(Locale.ROOT)) || !stations.findByNomIgnoreCase(nom).isEmpty()) {
                    throw erreur(ligne, "la station « " + nom + " » existe déjà.");
                }
                nouvellesStations.add(new Station(nom));
            } else {
                String reference = valeur(ligne, "reference", 50);
                if (!doublons.add(reference.toLowerCase(Locale.ROOT))
                        || equipements.existsByReferenceUniqueIgnoreCase(reference)) {
                    throw erreur(ligne, "la référence « " + reference + " » existe déjà.");
                }
                String libelle = valeur(ligne, "libelle", 10_000);
                String type = valeur(ligne, "type", 50);
                String nomStation = valeur(ligne, "station", 100);
                String etat = valeur(ligne, "etat", 50);
                List<Station> correspondances = stations.findByNomIgnoreCase(nomStation);
                if (correspondances.size() != 1) {
                    throw erreur(ligne, "station inconnue ou ambiguë « " + nomStation + " ». Importez d'abord les stations.");
                }
                nouveauxEquipements.add(new Equipement(reference, libelle, type, etat, correspondances.getFirst()));
            }
        }
        // Toutes les lignes sont validées avant la première insertion ; la transaction garantit le tout ou rien.
        if ("stations".equals(cible)) {
            stations.saveAllAndFlush(nouvellesStations);
        } else {
            equipements.saveAllAndFlush(nouveauxEquipements);
        }
        return lignes.size();
    }

    private List<CSVRecord> lire(MultipartFile fichier, List<String> colonnes) {
        if (fichier == null || fichier.isEmpty()) {
            throw new ImportCsvException("Sélectionnez un fichier CSV non vide.");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new ImportCsvException("Le fichier ne doit pas dépasser 1 Mio.");
        }
        try {
            String texte = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(fichier.getBytes())).toString();
            if (texte.startsWith("\uFEFF")) texte = texte.substring(1);
            CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader()
                    .setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).get();
            try (var parser = format.parse(new StringReader(texte))) {
                if (!parser.getHeaderNames().equals(colonnes)) {
                    throw new ImportCsvException("En-tête attendu : " + String.join(";", colonnes) + ".");
                }
                List<CSVRecord> lignes = parser.getRecords();
                if (lignes.isEmpty()) throw new ImportCsvException("Le fichier ne contient aucune donnée.");
                if (lignes.size() > LIGNES_MAX) throw new ImportCsvException("Maximum : 5 000 lignes de données.");
                for (CSVRecord ligne : lignes) {
                    if (ligne.size() != colonnes.size()) throw erreur(ligne, "nombre de colonnes incorrect.");
                }
                return lignes;
            }
        } catch (CharacterCodingException exception) {
            throw new ImportCsvException("Le fichier doit être enregistré en CSV UTF-8.");
        } catch (IOException | UncheckedIOException | IllegalArgumentException exception) {
            throw new ImportCsvException("CSV illisible : vérifiez les points-virgules et les guillemets.");
        }
    }

    private String valeur(CSVRecord ligne, String colonne, int maximum) {
        String valeur = ligne.get(colonne).strip();
        if (valeur.isEmpty() || valeur.length() > maximum || valeur.indexOf(0) >= 0) {
            throw erreur(ligne, "« " + colonne + " » est obligatoire et limité à " + maximum + " caractères.");
        }
        return valeur;
    }

    private ImportCsvException erreur(CSVRecord ligne, String message) {
        return new ImportCsvException("Ligne de données " + ligne.getRecordNumber() + " (hors en-tête) : " + message);
    }
}
