package fr.cytech.pau.hia_jee.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cytech.pau.hia_jee.model.Match;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.repository.MatchRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.service.TournamentService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/admin/tournaments")
//@RequiredArgsConstructor
public class TournamentController {
    private final TournamentRepository tRepo;
    private final MatchRepository mRepo;
    private final TournamentService tService;

    public TournamentController(TournamentRepository tRepo, MatchRepository mRepo, TournamentService tService) {
        this.tRepo = tRepo;
        this.mRepo = mRepo;
        this.tService = tService;
    }



    @GetMapping
    public String index(Model model,@RequestParam(required = false) String game){
        List<Tournament> tournaments;
       tournaments=tRepo.findAll();
       model.addAttribute("tournaments",tournaments);
       return "tournament_index";
        }
        @GetMapping("/{id}")
        public String view(@PathVariable Long id, Model model){
            Tournament tournament=tRepo.findById(id).orElseThrow(()->new RuntimeException("Tournoi introuvable"));
            model.addAttribute("tournament",tournament);
            model.addAttribute("matches",tournament.getMatches());
            return "tournament_view";
        }
        @PostMapping("/{id}/generate")
       public String generateBracket(@PathVariable Long id){
        tService.generateBracket(id);
        return "redirect:/admin/tournaments"+id;
       }
       @PostMapping("/match/{matchId}/score")
       public String enterScore(@PathVariable Long matchId,
        @RequestParam int scoreA, @RequestParam int scoreB){
            tService.enterScore(matchId, scoreA, scoreB);
            Match match=mRepo.findById(matchId).orElseThrow();
            Long tournamentId=match.getTournament().getId();
            return "redirect:/admin/tournaments"+tournamentId;

        }
        
    }
