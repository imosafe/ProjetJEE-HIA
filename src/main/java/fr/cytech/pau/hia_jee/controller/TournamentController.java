package fr.cytech.pau.hia_jee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.service.SponsorService;
import fr.cytech.pau.hia_jee.service.TournamentService;

/**
 * Contrôleur "Admin" pour la gestion des tournois.
 */
@Controller
@RequestMapping("/admin/tournaments")
public class TournamentController {

    private final TournamentRepository tRepo;
    private final TournamentService tournamentService;
    private final SponsorService sponsorService;

    public TournamentController(TournamentRepository tRepo, TournamentService tournamentService, SponsorService sponsorService) {
        this.tRepo = tRepo;
        this.tournamentService = tournamentService;
        this.sponsorService = sponsorService;
    }

    // ============================================================
    // 0. LISTE DES TOURNOIS
    // ============================================================

    /**
     * Cette méthode gère le GET sur /admin/tournaments (bouton Annuler).
     * Elle redirige vers la liste publique pour l'instant.
     */
    @GetMapping
    public String listTournaments() {
        return "redirect:/tournaments";
    }

    // ============================================================
    // 1. FORMULAIRE DE CRÉATION
    // ============================================================

    @GetMapping("/new")
    public String showTournamentForm(Model model) {
        model.addAttribute("tournament", new Tournament());
        model.addAttribute("allSponsors", sponsorService.findAll());
        return "admin/tournaments/form";
    }

    // ============================================================
    // 2. FORMULAIRE DE MODIFICATION
    // ============================================================

    @GetMapping("/modif/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tournament tournament = tRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        model.addAttribute("tournament", tournament);
        model.addAttribute("allSponsors", sponsorService.findAll());
        return "admin/tournaments/form";
    }

    // ============================================================
    // 3. SAUVEGARDE DU TOURNOI
    // ============================================================

    @PostMapping
    public String saveTournament(@ModelAttribute Tournament tournament) {
        tournamentService.save(tournament);
        return "redirect:/tournaments";
    }

    // ============================================================
    // 4. LOGIQUE BRACKET (GÉNÉRATION D'ARBRE)
    // ============================================================

    @PostMapping("/{id}/generate")
    public String generateBracket(@PathVariable Long id) {
        tournamentService.generateBracket(id);
        return "redirect:/tournaments/tree/" + id;
    }

    // ============================================================
    // 5. GESTION DES SCORES
    // ============================================================

    @PostMapping("/matches/score")
    public String enterScore(@RequestParam Long matchId,
                             @RequestParam int scoreA,
                             @RequestParam int scoreB,
                             RedirectAttributes redirectAttributes) {

        Long tournamentId = tournamentService.findTournamentIdByMatchId(matchId);

        try {
            tournamentService.enterScore(matchId, scoreA, scoreB);
            redirectAttributes.addFlashAttribute("successMessage", "Score enregistré !");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tournaments/tree/" + tournamentId;
    }
}