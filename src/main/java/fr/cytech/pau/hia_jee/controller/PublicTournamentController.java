package fr.cytech.pau.hia_jee.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.cytech.pau.hia_jee.model.Match;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.service.TournamentService;
import jakarta.servlet.http.HttpSession;

//Contrôleur gérant la partie PUBLIQUE des tournois, accessible à tous les visiteurs.

@Controller
@RequestMapping("/tournaments") 
public class PublicTournamentController {

    private final TournamentService tournamentService;
    private final TournamentRepository tournamentRepository;

    // Injection de dépendances via le constructeur (Bonne pratique Spring)
    public PublicTournamentController(TournamentService tournamentService, TournamentRepository tournamentRepository) {
        this.tournamentService = tournamentService;
        this.tournamentRepository = tournamentRepository;
    }

    // ============================================================
    // CATALOGUE (Liste des tournois)
    // ============================================================
    @GetMapping
    public String list(Model model) {
        // Récupération via le service
        List<Tournament> tournaments = tournamentService.findAll();
        model.addAttribute("tournaments", tournaments);
        
        return "tournament_index"; // Vue liste
    }

    // ============================================================
    // DÉTAILS D'UN TOURNOI
    // ============================================================

    @GetMapping("/view/{id:[0-9]+}")
    public String view(@PathVariable Long id, Model model) {
        
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        model.addAttribute("tournament", tournament);
        
        return "tournament_view"; 
    }

    // ============================================================
    // ARBRE DE TOURNOI (BRACKET)
    // ============================================================
   
    @GetMapping("/tree/{id}")
    public String showTree(@PathVariable Long id, Model model, HttpSession session){
        // 1. Récupération du tournoi
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        
        // 2. Récupérer tous les matchs
        // IMPORTANT : On copie dans une ArrayList pour éviter les problèmes Hibernate (LazyInitialization / PersistentBag)
        List<Match> allMatches = new ArrayList<>(tournament.getMatches());

        // 3. Organiser les matchs par Round
        // On utilise un TreeMap pour que les rounds soient triés automatiquement (1, 2, 3...)
        Map<Integer, List<Match>> matchesByRound = new java.util.TreeMap<>();

        if(allMatches != null && !allMatches.isEmpty()){
            
            // On parcourt simplement la liste des matchs existants
            for (Match match : allMatches) {
                // On récupère le numéro du round (stocké lors de la génération)
                Integer r = match.getRound();
                
                // Si la liste pour ce round n'existe pas encore, on la crée
                matchesByRound.putIfAbsent(r, new ArrayList<>());
                
                // On ajoute le match à la liste de son round
                matchesByRound.get(r).add(match);
            }
            
            // Optionnel : Trier les matchs à l'intérieur de chaque round par ID (pour l'affichage)
            for (List<Match> roundMatches : matchesByRound.values()) {
                roundMatches.sort((m1, m2) -> m1.getId().compareTo(m2.getId()));
            }
        }

        // 4. Envoi des données à la vue
        model.addAttribute("tournament", tournament);
        model.addAttribute("matchesByRound", matchesByRound);

        return "tournament_tree"; // Vue affichant l'arbre graphique
    }
}