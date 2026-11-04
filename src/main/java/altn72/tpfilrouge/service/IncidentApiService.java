package altn72.tpfilrouge.service;

import altn72.tpfilrouge.api.DetailIncidentApi;
import altn72.tpfilrouge.api.ResumeIncidentApi;
import altn72.tpfilrouge.repository.IncidentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('RESPONSABLE_EXPLOITATION', 'AGENT_SIGNALEMENT')")
public class IncidentApiService {
    private final IncidentService incidents;
    private final IncidentRepository repository;

    public IncidentApiService(IncidentService incidents, IncidentRepository repository) {
        this.incidents = incidents;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ResumeIncidentApi> lister() {
        return repository.findAllByOrderByDateHeureOuvertureDescIdDesc().stream()
                .map(ResumeIncidentApi::depuis).toList();
    }

    @Transactional(readOnly = true)
    public DetailIncidentApi consulter(Integer id) {
        return DetailIncidentApi.depuis(incidents.consulterDetail(id));
    }

    @Transactional
    @PreAuthorize("hasRole('RESPONSABLE_EXPLOITATION')")
    public DetailIncidentApi resoudre(Integer id) {
        incidents.resoudreIncident(id);
        return DetailIncidentApi.depuis(incidents.consulterDetail(id));
    }
}
