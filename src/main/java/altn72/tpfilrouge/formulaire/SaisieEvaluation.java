package altn72.tpfilrouge.formulaire;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SaisieEvaluation {
    @NotNull(message = "La note est obligatoire.")
    @Min(value = 1, message = "La note doit être comprise entre 1 et 5.")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5.")
    private Integer note;
    @Size(max = 255, message = "Le commentaire ne doit pas dépasser 255 caractères.")
    private String commentaire;

    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
