package fr.cytech.pau.hia_jee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cytech.pau.hia_jee.model.Sponsor;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

}
