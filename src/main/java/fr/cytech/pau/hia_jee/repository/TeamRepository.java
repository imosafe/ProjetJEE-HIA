package fr.cytech.pau.hia_jee.repository;

import fr.cytech.pau.hia_jee.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // Vérifie si une équipe porte déjà ce nom.
    
    boolean existsByName(String name);

    //Récupère une équipe par son nom exact.
    Team findByName(String name);

    // Récupère une équipe via son code d'invitation unique.
    Optional<Team> findByInviteCode(String inviteCode);

    // --- 2. REQUÊTE PERSONNALISÉE (JPQL) ---
    
    //Récupère une équipe ET ses membres en une seule requête SQL.
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);
}