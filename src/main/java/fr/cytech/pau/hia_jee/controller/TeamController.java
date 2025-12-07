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
        // Utilisation d'un service optimisé (avec Fetch Join si possible) pour charger les membres
        Team team = teamService.findTeamWithMembers(id);
        if (team == null) return "redirect:/teams";

        User sessionUser = (User) session.getAttribute("user");
        
        boolean isMember = false;
        boolean canJoin = false;
        boolean isLeader = false;

        if (sessionUser != null) {
            // On rafraîchit l'utilisateur depuis la BDD pour avoir ses relations à jour
            User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
            
            if (dbUser != null) {
                // Vérification stricte pour éviter NullPointerException
                if (dbUser.getTeam() != null) {
                    if (dbUser.getTeam().getId().equals(team.getId())) {
                        isMember = true;
                        // Vérifie si le leader n'est pas null avant de comparer
                        if (team.getLeader() != null && team.getLeader().getId().equals(dbUser.getId())) {
                            isLeader = true;
                        }
                    }
                } else {
                    // Si l'utilisateur n'a pas d'équipe, il est éligible pour rejoindre
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

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        // Si utilisateur introuvable ou sans équipe -> redirection vers création
        if (user == null || user.getTeam() == null) return "redirect:/teams/new";

        Team team = teamService.findTeamWithMembers(user.getTeam().getId());
        model.addAttribute("team", team);
        
        // Configuration des permissions pour la vue (C'est forcément un membre ici)
        boolean isLeader = (team.getLeader() != null && team.getLeader().getId().equals(user.getId()));
        model.addAttribute("isMember", true);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("canJoin", false);

        // --- FILTRAGE DES TOURNOIS DISPONIBLES ---
        List<Tournament> allUpcoming = tournamentRepository.findByStatus(StatusTournament.OUVERT); // Assurez-vous d'avoir OUVERT dans l'Enum
        List<Tournament> compatibleTournaments = new ArrayList<>();

        if (allUpcoming != null) {
            for (Tournament t : allUpcoming) {
                // Compatible si : Même jeu (Si défini) ET l'équipe n'est pas déjà inscrite
                boolean sameGame = (team.getGame() != null && team.getGame().equals(t.getGame()));
                
                // Vérification si l'équipe est déjà dans la liste des participants
                boolean notRegistered = t.getTeams().stream()
                        .noneMatch(registeredTeam -> registeredTeam.getId().equals(team.getId()));

                if (sameGame && notRegistered) {
                    compatibleTournaments.add(t);
                }
            }
        }

        model.addAttribute("availableTournaments", compatibleTournaments);
        
        // On réutilise la vue profile car elle est adaptative
        return "teams/profile"; // Ou "teams/my-team" si vous avez deux fichiers distincts
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
            
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);
            
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teams/create";
        }
    }

    // --- 4. ACTIONS ---

    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        
        if (user == null || user.getTeam() != null) {
            redirectAttributes.addFlashAttribute("error", "Vous avez déjà une équipe ou une erreur est survenue.");
            return "redirect:/teams/my";
        }

        try {
            // Note: userService.joinTeam ou teamService.addMember doit gérer la logique métier
            // Ici je simplifie en supposant une méthode directe ou via User
            Team teamToJoin = teamService.findById(id);
            if (teamToJoin != null) {
                user.setTeam(teamToJoin);
                userRepository.save(user);
                
                // Mise à jour session
                sessionUser.setTeam(teamToJoin);
                session.setAttribute("user", sessionUser);
                
                redirectAttributes.addFlashAttribute("success", "Bienvenue chez " + teamToJoin.getName() + " !");
                return "redirect:/teams/my";
            }
        } catch (Exception e) {
             redirectAttributes.addFlashAttribute("error", "Erreur lors de l'adhésion: " + e.getMessage());
        }

        return "redirect:/teams";
    }

    @PostMapping("/register/{tournamentId}")
    public String registerToTournament(@PathVariable Long tournamentId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null || user.getTeam() == null) return "redirect:/teams/new";

        try {
            teamService.registerTeamToTournament(user.getTeam().getId(), tournamentId, user);
            redirectAttributes.addFlashAttribute("success", "Inscription confirmée !");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Échec inscription : " + e.getMessage());
        }
        return "redirect:/teams/my";
    }

    @PostMapping("/leave")
    public String leaveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            try {
                userService.leaveTeam(sessionUser.getId());
                
                sessionUser.setTeam(null);
                session.setAttribute("user", sessionUser);
                
                redirectAttributes.addFlashAttribute("success", "Vous avez quitté l'équipe.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/teams/my";
            }
        }
        return "redirect:/teams";
    }

    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";
        
        User dbUser = userRepository.findById(sessionUser.getId()).orElseThrow();
        
        // Vérification stricte des droits
        if (dbUser.getTeam() != null && dbUser.getTeam().getLeader() != null 
            && dbUser.getTeam().getLeader().getId().equals(dbUser.getId())) {
            
            teamService.dissolveTeam(dbUser.getTeam().getId());
            
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);
            
            redirectAttributes.addFlashAttribute("success", "L'équipe a été dissoute.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Action non autorisée.");
            return "redirect:/teams/my";
        }
        return "redirect:/teams";
    }

    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            try { 
                userService.kickMember(sessionUser.getId(), memberId);
                redirectAttributes.addFlashAttribute("success", "Membre exclu avec succès.");
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
            
            // Récupération de l'équipe pour mettre à jour la session
            Team t = teamService.findByInviteCode(code); 
            sessionUser.setTeam(t);
            session.setAttribute("user", sessionUser);
            
            redirectAttributes.addFlashAttribute("success", "Invitation acceptée !");
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams";
        }
    }
}