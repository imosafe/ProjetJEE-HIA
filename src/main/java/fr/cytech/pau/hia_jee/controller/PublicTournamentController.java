package fr.cytech.pau.hia_jee.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.service.TournamentService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tournaments")
public class PublicTournamentController {

    private final TournamentService tournamentService;

    public PublicTournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // Liste publique (catalogue)
    @GetMapping
    public String list(Model model, HttpSession session) {
        // Exiger un utilisateur connecté
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        List<Tournament> tournaments = tournamentService.findAll();
        model.addAttribute("tournaments", tournaments);
        return "tournament_index"; // template existant
    }

    // Vue publique d'un tournoi
    @GetMapping("/{id:[0-9]+}")
    public String view(@PathVariable Long id, Model model, HttpSession session) {
        // Exiger un utilisateur connecté
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        model.addAttribute("tournament", tournament);
        return "tournament_view"; // template minimal ajouté ci-dessous
    }
}
