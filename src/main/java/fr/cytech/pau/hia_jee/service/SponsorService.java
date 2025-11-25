
package fr.cytech.pau.hia_jee.service;

import fr.cytech.pau.hia_jee.model.Sponsor;
import fr.cytech.pau.hia_jee.repository.SponsorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SponsorService {

    @Autowired 
    private SponsorRepository sponsorRepository;

  
    public Sponsor save(Sponsor sponsor) {
        // Logique Métier future pourrait être ici :
         
        return sponsorRepository.save(sponsor); 
    }

    /**
     * Retourne tous les sponsors.
     */
    public List<Sponsor> findAll() {
        return sponsorRepository.findAll();
    }

    /**
     * Retourne un sponsor par son ID.
     */
    public Optional<Sponsor> findById(Long id) {
        return sponsorRepository.findById(id);
    }

    /**
     * Supprime un sponsor.
     */
    public void deleteById(Long id) {
        sponsorRepository.deleteById(id);
    }
}