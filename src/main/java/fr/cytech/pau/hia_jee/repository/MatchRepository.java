package fr.cytech.pau.hia_jee.repository;
import fr.cytech.pau.hia_jee.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match,Long> {
}