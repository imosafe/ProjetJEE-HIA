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
// On n'a plus besoin de HttpSession pour vérifier le login ici
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/tournaments")
public class PublicTournamentController {

    private final TournamentService tournamentService;
    private final TournamentRepository tournamentRepository;

    public PublicTournamentController(TournamentService tournamentService, TournamentRepository tournamentRepository) {
        this.tournamentService = tournamentService;
        this.tournamentRepository=tournamentRepository;
    }

    // ==========================================
    // CATALOGUE (ACCESSIBLE À TOUS)
    // ==========================================
    @GetMapping
    public String list(Model model) {
        // J'AI SUPPRIMÉ LA VÉRIFICATION DE SESSION ICI
        // Maintenant, tout le monde peut voir la liste
        
        List<Tournament> tournaments = tournamentService.findAll();
        model.addAttribute("tournaments", tournaments);
        
        return "tournament_index"; 
    }

    
    @GetMapping("/view/{id:[0-9]+}")
    public String view(@PathVariable Long id, Model model) {
        // J'AI SUPPRIMÉ LA VÉRIFICATION DE SESSION ICI AUSSI
        // Un visiteur doit pouvoir voir les détails du match sans être connecté
        
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        model.addAttribute("tournament", tournament);
        
        return "tournament_view"; 
    }
    // ---7. Afficher l'arbre du tournoi
    @GetMapping("/tree/{id}")
    public String showTree(@PathVariable Long id, Model model, HttpSession session){
        Tournament tournament =tournamentRepository.findById(id).orElseThrow(()->new RuntimeException("Tournoi introuvable"));
        //Recupérer tous les matchs
        List<Match> allMatches=tournament.getMatches();
        //Logique de découpage en rounds
        Map<Integer, List<Match>> matchesByRound=new LinkedHashMap<>();
        if(allMatches!=null && !allMatches.isEmpty()){
            //On s'assure qu'ils sont triés par ID
            allMatches.sort((m1,m2)->m1.getId().compareTo(m2.getId()));
            int matchesInCurrentRound=(allMatches.size()+1)/2;
            int index=0;
            int roundNumber=1;
            //On découpe la liste morceau par morceau
            while(index<allMatches.size()){
                //on prend la sous-liste pour ce round
                int endIndex=Math.min(index+matchesInCurrentRound, allMatches.size());
                List<Match> roundMatches=allMatches.subList(index, endIndex);
                matchesByRound.put(roundNumber, roundMatches);
                //Préparation pour le tour suivant
                index=endIndex;
                matchesInCurrentRound=matchesInCurrentRound/2;
                roundNumber++;
            }
        }
        // envoi des données à la vue
        model.addAttribute("tournament", tournament);
        model.addAttribute("matchesByRound", matchesByRound);

        return "tournament_tree";
    }
}