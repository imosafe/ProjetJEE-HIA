package fr.cytech.pau.hia_jee.controller;

import java.util.LinkedHashMap;
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
        
        // 2. Récupérer tous les matchs associés au tournoi
        List<Match> allMatches = tournament.getMatches();

        // Map pour stocker les matchs par round :
        // Clé = Numéro du round (1, 2, 3...)
        // Valeur = Liste des matchs de ce round
        // LinkedHashMap est utilisé pour garantir l'ordre d'insertion (Round 1, puis 2, puis 3...)
        Map<Integer, List<Match>> matchesByRound = new LinkedHashMap<>();

        if(allMatches != null && !allMatches.isEmpty()){
            
            // 3. Tri des matchs par ID croissant.
            // HYPOTHÈSE : Les matchs ont été créés dans l'ordre chronologique en base de données.
            // ID petits = 1er tour, ID moyens = 2e tour, ID grand = Finale.
            allMatches.sort((m1, m2) -> m1.getId().compareTo(m2.getId()));

            // 4. Algorithme de découpage par puissance de 2
            // Dans un tournoi à élimination directe complet, le nombre de matchs total est N-1 (où N est le nb d'équipes).
            // Le 1er tour contient environ la moitié des matchs totaux + 1.
            int matchesInCurrentRound = (allMatches.size() + 1) / 2;
            
            int index = 0;          // Curseur pour parcourir la liste principale
            int roundNumber = 1;    // Compteur de rounds

            // On boucle tant qu'on n'a pas traité tous les matchs de la liste
            while(index < allMatches.size()){
                
                // Calcul de l'index de fin pour le round actuel
                // Math.min évite de dépasser la taille de la liste (IndexOutOfBoundsException)
                int endIndex = Math.min(index + matchesInCurrentRound, allMatches.size());
                
                // Extraction de la sous-liste correspondant au Round actuel
                List<Match> roundMatches = allMatches.subList(index, endIndex);
                
                // Stockage dans la Map
                matchesByRound.put(roundNumber, roundMatches);

                // --- Préparation pour le tour suivant ---
                
                // On avance le curseur à la fin du round qu'on vient de traiter
                index = endIndex;
                
                // Le nombre de matchs est divisé par 2 à chaque tour suivant (8e -> quarts -> demies -> finale)
                matchesInCurrentRound = matchesInCurrentRound / 2;
                
                // On passe au round suivant
                roundNumber++;
            }
        }

        // 5. Envoi des données à la vue
        model.addAttribute("tournament", tournament);
        model.addAttribute("matchesByRound", matchesByRound);

        return "tournament_tree"; // Vue affichant l'arbre graphique
    }
}