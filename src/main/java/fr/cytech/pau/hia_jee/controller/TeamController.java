package fr.cytech.pau.hia_jee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.cytech.pau.hia_jee.model.StatusTournament;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import fr.cytech.pau.hia_jee.service.TeamService;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/teams")
public class TeamController {

    @Autowired private TeamService teamService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private TournamentRepository tournamentRepository;

    // --- 1. LISTE ET PROFIL PUBLIC ---

    @GetMapping("")
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAllTeams());
        return "teams/list";
    }

    @GetMapping("/{id}")
    public String showTeamProfile(@PathVariable Long id, Model model, HttpSession session) {
        Team team = teamService.findTeamWithMembers(id);
        if (team == null) return "redirect:/teams";

        User sessionUser = (User) session.getAttribute("user");
        
        boolean isMember = false;
        boolean canJoin = false;
        boolean isLeader = false;

        if (sessionUser != null) {
            // On rafraichit l'user depuis la BDD pour être sûr de son état actuel
            User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
            
            if (dbUser != null) {
                if (dbUser.getTeam() != null && dbUser.getTeam().getId().equals(team.getId())) {
                    isMember = true;
                    if (team.getLeader() != null && team.getLeader().getId().equals(dbUser.getId())) {
                        isLeader = true;
                    }
                } else if (dbUser.getTeam() == null) {
                    canJoin = true;
                }
            }
        }

        model.addAttribute("team", team);
        model.addAttribute("isMember", isMember);
        model.addAttribute("canJoin", canJoin);
        model.addAttribute("isLeader", isLeader);

        return "teams/profile"; 
    }

    // --- 2. TABLEAU DE BORD "MON ÉQUIPE" ---

    @GetMapping("/my")
    public String myTeam(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Récupération fraîche depuis la BDD
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) return "redirect:/login";

        // Si l'utilisateur n'a pas d'équipe, on le redirige vers la création
        if (user.getTeam() == null) return "redirect:/teams/new";

        Team team = teamService.findTeamWithMembers(user.getTeam().getId());
        model.addAttribute("team", team);

        // --- FILTRAGE DES TOURNOIS DISPONIBLES ---
        List<Tournament> allUpcoming = tournamentRepository.findByStatus(StatusTournament.OUVERT);
        List<Tournament> compatibleTournaments = new ArrayList<>();

        for (Tournament t : allUpcoming) {
            // Compatible si : Même jeu ET l'équipe n'est pas déjà inscrite
            boolean sameGame = (t.getGame() == team.getGame());
            boolean notRegistered = !t.getTeams().contains(team);

            if (sameGame && notRegistered) {
                compatibleTournaments.add(t);
            }
        }

        model.addAttribute("availableTournaments", compatibleTournaments);
        return "teams/my-team";
    }

    // --- 3. CRÉATION D'ÉQUIPE ---

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";
        
        User dbUser = userRepository.findById(sessionUser.getId()).orElseThrow();
        if (dbUser.getTeam() != null) return "redirect:/teams/my";

        model.addAttribute("team", new Team());
        return "teams/create";
    }

    @PostMapping("/new")
    public String processCreate(@ModelAttribute Team team, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            Team savedTeam = teamService.createTeam(team, sessionUser);
            
            // Mise à jour de la session
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);
            
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teams/create";
        }
    }

    // --- 4. ACTIONS (Rejoindre, Quitter, Inscription Tournoi) ---

    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null || user.getTeam() != null) {
            redirectAttributes.addFlashAttribute("error", "Impossible de rejoindre l'équipe.");
            return "redirect:/teams/my";
        }

        Team teamToJoin = teamService.findById(id);
        if (teamToJoin != null) {
            user.setTeam(teamToJoin);
            userRepository.save(user); // Sauvegarde BDD
            
            // Mise à jour session pour affichage immédiat
            sessionUser.setTeam(teamToJoin);
            session.setAttribute("user", sessionUser);
            
            redirectAttributes.addFlashAttribute("success", "Bienvenue dans l'équipe " + teamToJoin.getName());
        }

        return "redirect:/teams/my";
    }

    @PostMapping("/register/{tournamentId}")
    public String registerToTournament(@PathVariable Long tournamentId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null || user.getTeam() == null) return "redirect:/teams/new";

        try {
            teamService.registerTeamToTournament(user.getTeam().getId(), tournamentId, user);
            redirectAttributes.addFlashAttribute("success", "Inscription au tournoi réussie !");
        } catch (RuntimeException e) {
            // Utilisation de FlashAttribute pour afficher l'erreur après la redirection
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teams/my";
    }

    @PostMapping("/leave")
    public String leaveTeam(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            userService.leaveTeam(sessionUser.getId());
            
            // Mise à jour session
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);
        }
        return "redirect:/teams"; // Rediriger vers la liste des équipes plutôt que l'accueil
    }

    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";
        
        // Vérification stricte en BDD
        User dbUser = userRepository.findById(sessionUser.getId()).orElseThrow();
        
        if (dbUser.getTeam() != null && dbUser.getTeam().getLeader().getId().equals(dbUser.getId())) {
            teamService.dissolveTeam(dbUser.getTeam().getId());
            
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);
        }
        return "redirect:/teams";
    }

    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            try { 
                userService.kickMember(sessionUser.getId(), memberId);
                redirectAttributes.addFlashAttribute("success", "Membre exclu.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/teams/my";
    }

    @GetMapping("/invite/{code}")
    public String joinByInvite(@PathVariable String code, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";
        
        try {
            userService.joinTeamByInviteCode(sessionUser.getId(), code);
            
            // Mise à jour session pour refléter le changement immédiat
            Team t = teamService.findByInviteCode(code);
            sessionUser.setTeam(t);
            session.setAttribute("user", sessionUser);
            
            redirectAttributes.addFlashAttribute("success", "Vous avez rejoint l'équipe via invitation !");
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams";
        }
    }
}