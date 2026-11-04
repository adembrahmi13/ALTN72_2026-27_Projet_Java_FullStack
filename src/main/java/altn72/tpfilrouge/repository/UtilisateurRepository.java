package altn72.tpfilrouge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import altn72.tpfilrouge.modele.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByLogin(String login);
}