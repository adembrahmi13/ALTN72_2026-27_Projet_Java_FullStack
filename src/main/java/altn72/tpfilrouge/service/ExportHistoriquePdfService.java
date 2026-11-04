package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
public class ExportHistoriquePdfService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final IncidentService incidents;
    private final Clock horloge;

    public ExportHistoriquePdfService(IncidentService incidents, Clock horloge) {
        this.incidents = incidents;
        this.horloge = horloge;
    }

    public byte[] exporter() throws IOException {
        var historique = incidents.consulterHistorique();
        try (PDDocument document = new PDDocument(); var sortie = new ByteArrayOutputStream()) {
            document.getDocumentInformation().setTitle("Guardian - Historique des incidents résolus");
            try (var rendu = new Rendu(document)) {
                rendu.ligne("Export du " + DATE.format(LocalDateTime.now(horloge)));
                rendu.ligne(historique.size() + " incident(s) - résolution la plus récente en premier");
                rendu.ligne("");
                if (historique.isEmpty()) rendu.ligne("Aucun incident résolu pour le moment.");
                for (Incident incident : historique) {
                    rendu.ligne("Incident n° " + incident.getId() + " - Résolu");
                    rendu.ligne("Description : " + incident.getDescription());
                    rendu.ligne("Station : " + incident.getEquipement().getStation().getNom());
                    rendu.ligne("Équipement : " + incident.getEquipement().getReferenceUnique()
                            + " - " + incident.getEquipement().getLibelleDescription());
                    rendu.ligne("Ouverture : " + DATE.format(incident.getDateHeureOuverture()));
                    rendu.ligne("Résolution : " + DATE.format(incident.getDateHeureResolution()));
                    rendu.ligne("Durée : " + incidents.calculerDureeOuverture(incident));
                    rendu.ligne("Prestataire : " + (incident.getPrestataire() == null
                            ? "Sans prestataire" : incident.getPrestataire().getNom()));
                    rendu.ligne("");
                }
            }
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                try (var flux = new PDPageContentStream(document, document.getPage(i),
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    flux.beginText();
                    flux.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                    flux.newLineAtOffset(45, 27);
                    flux.showText("Guardian - Page " + (i + 1) + " / " + document.getNumberOfPages());
                    flux.endText();
                }
            }
            document.save(sortie);
            return sortie.toByteArray();
        }
    }

    // Retour à la ligne par largeur réelle et nouvelle page avant de sortir de la zone imprimable.
    private static class Rendu implements AutoCloseable {
        private final PDDocument document;
        private final PDFont police = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private PDPageContentStream flux;
        private float y;

        Rendu(PDDocument document) throws IOException {
            this.document = document;
            nouvellePage();
        }

        private void nouvellePage() throws IOException {
            if (flux != null) flux.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            flux = new PDPageContentStream(document, page);
            y = 785;
            ecrire("Guardian - Historique des incidents résolus");
            y -= 12;
        }

        void ligne(String texte) throws IOException {
            StringBuilder courant = new StringBuilder();
            for (int point : texte.codePoints().toArray()) {
                String caractere = Character.isISOControl(point) ? " " : new String(Character.toChars(point));
                try {
                    police.encode(caractere);
                } catch (IllegalArgumentException exception) {
                    caractere = "?"; // Polices PDF standard : remplacement des caractères hors alphabet pris en charge.
                }
                if (police.getStringWidth(courant + caractere) / 1000 * 11 > 505 && !courant.isEmpty()) {
                    ecrire(courant.toString());
                    courant.setLength(0);
                }
                courant.append(caractere);
            }
            ecrire(courant.toString());
        }

        private void ecrire(String texte) throws IOException {
            if (y < 60) nouvellePage();
            flux.beginText();
            flux.setFont(police, 11);
            flux.newLineAtOffset(45, y);
            flux.showText(texte);
            flux.endText();
            y -= 16;
        }

        @Override
        public void close() throws IOException {
            if (flux != null) flux.close();
        }
    }
}
