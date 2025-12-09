package fr.cytech.pau.hia_jee.repository;
import fr.cytech.pau.hia_jee.model.StatusTournament;
import fr.cytech.pau.hia_jee.model.Tournament;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament,Long> {
    
    //Récupère un tournois par son status.
    List<Tournament> findByStatus(StatusTournament status);
}