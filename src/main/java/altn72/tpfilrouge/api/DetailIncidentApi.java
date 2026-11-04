package altn72.tpfilrouge.api;

import java.time.LocalDate;
import altn72.tpfilrouge.service.DetailIncident;

public record DetailIncidentApi(ResumeIncidentApi incident, String dureeOuverture, EvaluationApi evaluation) {
    public record EvaluationApi(Integer note, String commentaire, LocalDate date) {}

    public static DetailIncidentApi depuis(DetailIncident detail) {
        var evaluation = detail.evaluation();
        return new DetailIncidentApi(ResumeIncidentApi.depuis(detail.incident()), detail.dureeOuverture(),
                evaluation == null ? null : new EvaluationApi(evaluation.getNote(), evaluation.getCommentaire(), evaluation.getDate()));
    }
}
