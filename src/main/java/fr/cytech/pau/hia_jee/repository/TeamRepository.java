package fr.cytech.pau.hia_jee.repository;

import fr.cytech.pau.hia_jee.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // Utile lors de la création d'équipe : Vérifier que le nom n'existe pas déjà
    boolean existsByName(String name);

    // Si tu as besoin de chercher une équipe par son nom exact
    Team findByName(String name);
}