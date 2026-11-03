package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DroitsIncident {
    private boolean possedeRole(String role) {
        Authentication utilisateur = SecurityContextHolder.getContext().getAuthentication();
        return utilisateur != null && utilisateur.isAuthenticated()
                && utilisateur.getAuthorities().stream()
                .anyMatch(autorite -> autorite.getAuthority().equals("ROLE_" + role));
    }

    public boolean peutModifier(Incident incident) {
        return (possedeRole("RESPONSABLE_EXPLOITATION") || possedeRole("AGENT_SIGNALEMENT"))
                && incident.getDateHeureResolution() == null
                && !Incident.STATUT_RESOLU.equals(incident.getStatut());
    }

    public boolean peutResoudre(Incident incident) {
        return possedeRole("RESPONSABLE_EXPLOITATION") && peutModifier(incident);
    }

    public boolean peutChangerEquipement(Incident incident) {
        return peutModifier(incident) && possedeRole("RESPONSABLE_EXPLOITATION")
                && incident.getPrestataireId() == null
                && Incident.STATUT_NON_AFFECTE.equals(incident.getStatut());
    }
}
