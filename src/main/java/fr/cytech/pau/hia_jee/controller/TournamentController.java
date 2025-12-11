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
 * Contrôleur "Admin" pour la gestion des tournois, accessible qu'aux admins.
 */
@Controller
@RequestMapping("/admin/tournaments")
public class TournamentController {

    private final TournamentRepository tRepo;
    private final TournamentService tService;
    private final SponsorService sponsorService;
    private final TournamentService tournamentService;

    // Injection par constructeur : meilleure pratique que @Autowired sur les champs
    // Cela rend le contrôleur plus facile à tester unitairement.
    public TournamentController(TournamentService tournamentService, TournamentRepository tRepo, TournamentService tService, SponsorService sponsorService) {
        this.tRepo = tRepo;
        this.tService = tService;
        this.sponsorService = sponsorService;
        this.tournamentService=tournamentService;
    }

    // ============================================================
    // 1. FORMULAIRE DE CRÉATION
    // ============================================================
    
    /**
     * Affiche le formulaire vierge pour créer un tournoi.
     */
    @GetMapping("/new")
    public String showTournamentForm(Model model) {
        // On injecte un objet vide pour le binding du formulaire
        model.addAttribute("tournament", new Tournament());
        
        // On injecte la liste des sponsors pour pouvoir les cocher dans le formulaire
        model.addAttribute("allSponsors", sponsorService.findAll());
        
        return "admin/tournaments/form"; // Vue partagée (création/édition)
    }

    // ============================================================
    // 2. FORMULAIRE DE MODIFICATION
    // ============================================================

    /**
     * Affiche le formulaire pré-rempli pour modifier un tournoi existant.
     */
    @GetMapping("/modif/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Récupération du tournoi existant
        Tournament tournament = tRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));
        
        model.addAttribute("tournament", tournament);
        
        // On a besoin de la liste des sponsors pour pré-cocher ceux qui sont déjà sélectionnés
        model.addAttribute("allSponsors", sponsorService.findAll());
        
        // On réutilise le même template HTML que pour la création (DRY: Don't Repeat Yourself)
        return "admin/tournaments/form"; 
    }

    // ============================================================
    // 3. SAUVEGARDE DU TOURNOI
    // ============================================================

    /**
     * Traite la soumission du formulaire (Création OU Modification).
     */
    @PostMapping
    public String saveTournament(@ModelAttribute Tournament tournament) {
        // Le service gère la logique : 
        // - Si ID est null -> INSERT
        // - Si ID existe -> UPDATE
        tService.save(tournament); 
        
        // Redirection vers la liste publique des tournois pour vérifier le résultat
        return "redirect:/tournaments"; 
    }

    // ============================================================
    // 4. LOGIQUE BRACKET (GÉNÉRATION D'ARBRE)
    // ============================================================

    /**
     * Déclenche la génération de l'arbre de tournoi.
     * Cette action clôture les inscriptions et crée les objets Match en base de données.
     */
    @PostMapping("/{id}/generate")
    public String generateBracket(@PathVariable Long id) {
        // Appel de la logique métier complexe (mélange des équipes, création des matchs par puissance de 2)
        tService.generateBracket(id);
        
        // Redirection immédiate vers la vue "Arbre" pour voir le résultat visuel
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

        // 1. Récupérer l'ID du tournoi pour la redirection
        Long tournamentId = tournamentService.findTournamentIdByMatchId(matchId);

        try {
            // 2. Appel au service (qui contient la logique métier)
            tournamentService.enterScore(matchId, scoreA, scoreB);
            
            // Succès
            redirectAttributes.addFlashAttribute("successMessage", "Score enregistré et tableau mis à jour !");

        } catch (RuntimeException e) {
            // 3. Gestion de l'erreur (ex: Match nul)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // 4. Retour à la page de l'arbre
        return "redirect:/tournaments/tree/" + tournamentId;
    }
}