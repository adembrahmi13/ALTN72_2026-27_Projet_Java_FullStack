package altn72.tpfilrouge.repository;

import altn72.tpfilrouge.modele.Prestataire;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestataireRepository extends JpaRepository<Prestataire, Integer> {
    // Récupération tous les prestataires 
    List<Prestataire> findAll();

    // Récupération d'un prestataire par son ID
    public Optional<Prestataire> findById(Integer id);

    //Enregistrer un prestataire
    public Prestataire save(Prestataire prestataire);
    
}
