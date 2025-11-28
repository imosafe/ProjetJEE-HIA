// src/main/java/fr/cytech/pau/hia_jee/repository/MatchRepository.java

package fr.cytech.pau.hia_jee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cytech.pau.hia_jee.model.Match; 

@Repository
public interface MatchRepository extends JpaRepository<Match,Long> {
}