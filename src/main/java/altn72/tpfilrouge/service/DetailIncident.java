package altn72.tpfilrouge.service;

import altn72.tpfilrouge.modele.Evaluation;
import altn72.tpfilrouge.modele.Incident;

public record DetailIncident(Incident incident, String dureeOuverture, Evaluation evaluation) {}

