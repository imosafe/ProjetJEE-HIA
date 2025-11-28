package fr.cytech.pau.hia_jee.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.cytech.pau.hia_jee.model.Sponsor;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.SponsorRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;

@Service
//@RequiredArgsConstructor
public class SponsorService {

    private final SponsorRepository sponsorRepository;
    // Injection nécessaire pour mettre à jour la table de liaison Tournament_Sponsor
    private final TournamentRepository tournamentRepository;

    public SponsorService(SponsorRepository sponsorRepository, TournamentRepository tournamentRepository) {
        this.sponsorRepository = sponsorRepository;
        this.tournamentRepository = tournamentRepository;
    }
    


    // 1. Trouver tous les sponsors
    public List<Sponsor> findAll() {
        return sponsorRepository.findAll();
    }

    // 2. Trouver un sponsor par ID
    public Optional<Sponsor> findById(Long id) {
        return sponsorRepository.findById(id);
    }

    // 3. Sauvegarder un sponsor
    public Sponsor save(Sponsor sponsor) {
        return sponsorRepository.save(sponsor);
    }

    // 4. Supprimer un sponsor (Avec nettoyage des relations N-N)
    public void deleteById(Long id) {
        // On récupère d'abord le sponsor pour accéder à ses tournois
        Sponsor sponsor = sponsorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sponsor introuvable avec l'id : " + id));

        // NETTOYAGE : On parcourt chaque tournoi lié à ce sponsor
        // et on retire le sponsor de la liste du tournoi.
        for (Tournament tournament : sponsor.getTournaments()) {
            tournament.getSponsors().remove(sponsor);
            tournamentRepository.save(tournament); // Sauvegarde la mise à jour dans la table de jointure
        }

        // Une fois détaché de partout, on peut le supprimer sans erreur SQL
        sponsorRepository.deleteById(id);
    }
}