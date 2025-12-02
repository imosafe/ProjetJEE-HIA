package fr.cytech.pau.hia_jee.repository;

import fr.cytech.pau.hia_jee.model.Sponsor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

    // Cette requête cherche le mot clé dans le NOM, le TYPE ou le NIVEAU
    // Elle ignore la casse (minuscule/majuscule)
    @Query("SELECT s FROM Sponsor s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.type AS string) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.level AS string) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<Sponsor> search(@Param("keyword") String keyword, Pageable pageable);
}