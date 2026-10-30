package altn72.tpfilrouge.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Entity
@Table(name = "evaluation")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(optional = false)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer note;
    private String commentaire;
    @Column(nullable = false)
    private LocalDate date;

    public Evaluation() {}

    public Evaluation(Incident incident, Integer note, String commentaire, LocalDate date) {
        this.incident = incident;
        this.note = note;
        this.commentaire = commentaire;
        this.date = date;
    }

    public Integer getId() { return id; }
    public Incident getIncident() { return incident; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDate getDate() { return date; }
}

