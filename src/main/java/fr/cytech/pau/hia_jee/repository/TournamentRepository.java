package fr.cytech.pau.hia_jee.repository;
import fr.cytech.pau.hia_jee.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament,Long> {
}