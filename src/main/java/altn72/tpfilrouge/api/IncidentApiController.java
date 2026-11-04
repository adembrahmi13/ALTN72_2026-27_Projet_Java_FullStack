package altn72.tpfilrouge.api;

import altn72.tpfilrouge.service.IncidentApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Incidents", description = "Consultation par les deux profils ; résolution réservée au responsable.")
@SecurityRequirement(name = "session")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Connexion requise"),
        @ApiResponse(responseCode = "403", description = "Profil non autorisé ou jeton CSRF invalide")
})
public class IncidentApiController {
    private final IncidentApiService incidents;

    public IncidentApiController(IncidentApiService incidents) {
        this.incidents = incidents;
    }

    @GetMapping("/incidents")
    @Operation(summary = "Lister les incidents", description = "Incidents ouverts et résolus, triés par ouverture décroissante.")
    @ApiResponse(responseCode = "200", description = "Liste JSON, vide si aucun incident")
    public List<ResumeIncidentApi> lister() {
        return incidents.lister();
    }

    @GetMapping("/incidents/{id}")
    @Operation(summary = "Consulter le détail d'un incident")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident, durée et évaluation éventuelle"),
            @ApiResponse(responseCode = "400", description = "Identifiant non numérique"),
            @ApiResponse(responseCode = "404", description = "Incident introuvable")
    })
    public DetailIncidentApi consulter(@PathVariable Integer id) {
        return incidents.consulter(id);
    }

    @PostMapping("/incidents/{id}/resoudre")
    @Operation(summary = "Déclarer un incident résolu", description = """
            Responsable uniquement. Aucun corps à envoyer : date et heure fixées côté serveur.
            Une session authentifiée et un jeton CSRF sont requis. Swagger ajoute automatiquement
            l'en-tête X-XSRF-TOKEN à partir du cookie XSRF-TOKEN.
            Pour un autre client, GET /api/csrf fournit le jeton à renvoyer avec les cookies de session.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Détail de l'incident résolu"),
            @ApiResponse(responseCode = "400", description = "Identifiant non numérique"),
            @ApiResponse(responseCode = "404", description = "Incident introuvable"),
            @ApiResponse(responseCode = "409", description = "Incident déjà résolu ou résolution antérieure à l'ouverture")
    })
    public DetailIncidentApi resoudre(@PathVariable Integer id) {
        return incidents.resoudre(id);
    }

    public record JetonCsrf(String headerName, String token) {}

    @GetMapping("/csrf")
    @Operation(summary = "Obtenir le jeton CSRF de la session",
            description = "Renvoyer le jeton dans l'en-tête indiqué pour les POST, en conservant les cookies de session.")
    @ApiResponse(responseCode = "200", description = "Nom de l'en-tête et jeton CSRF")
    public JetonCsrf csrf(@Parameter(hidden = true) CsrfToken token) {
        return new JetonCsrf(token.getHeaderName(), token.getToken());
    }
}
