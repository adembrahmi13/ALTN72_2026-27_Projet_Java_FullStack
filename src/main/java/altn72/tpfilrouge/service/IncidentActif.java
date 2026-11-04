package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Incident;
import altn72.tpfilrouge.modele.NiveauAttention;

// Une ligne du tableau de bord : l'incident, son niveau d'attention et sa durée d'ouverture
public record IncidentActif(Incident incident, NiveauAttention niveau, String dureeOuverture) {}
