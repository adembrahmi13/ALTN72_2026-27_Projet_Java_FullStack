package altn72.tpfilrouge.formulaire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ModificationIncident {
    @NotBlank(message = "La description est obligatoire.")
    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères.")
    private String description;
    @NotNull(message = "Sélectionnez un équipement.")
    private Integer equipementId;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getEquipementId() { return equipementId; }
    public void setEquipementId(Integer equipementId) { this.equipementId = equipementId; }
}
