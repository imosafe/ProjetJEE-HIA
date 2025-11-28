// src/main/java/fr/cytech.pau.hia_jee.repository/TournamentRepository.java

package fr.cytech.pau.hia_jee.repository; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cytech.pau.hia_jee.model.Tournament; 

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {}