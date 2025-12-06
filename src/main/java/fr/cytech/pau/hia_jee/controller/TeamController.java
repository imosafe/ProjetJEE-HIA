package fr.cytech.pau.hia_jee.controller;

import fr.cytech.pau.hia_jee.model.StatusTournament;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import fr.cytech.pau.hia_jee.service.TeamService;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/teams")
public class TeamController {

    @Autowired private TeamService teamService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private TournamentRepository tournamentRepository;

    // --- 1. MON ÉQUIPE ---
    @GetMapping("/my")
    public String myTeam(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) return "redirect:/login";
        if (user.getTeam() == null) return "redirect:/teams/new";

        Team team = teamService.findTeamWithMembers(user.getTeam().getId());
        model.addAttribute("team", team);

        // --- FILTRAGE DES TOURNOIS PAR ENUM ---
        // 1. On récupère tous les tournois "À venir"
        List<Tournament> allUpcoming = tournamentRepository.findByStatus(StatusTournament.OUVERT);
        List<Tournament> compatibleTournaments = new ArrayList<>();

        // 2. On filtre manuellement (Plus sûr pour le debug)
        for (Tournament t : allUpcoming) {
            // Comparaison stricte d'Enum (Game == Game)
            boolean sameGame = (t.getGame() == team.getGame());
            boolean notRegistered = !t.getTeams().contains(team);

            if (sameGame && notRegistered) {
                compatibleTournaments.add(t);
            }
        }

        model.addAttribute("availableTournaments", compatibleTournaments);
        return "teams/my-team";
    }

    // --- 2. CRÉATION ---
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        User dbUser = userRepository.findById(user.getId()).orElseThrow();
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
            userService.joinTeam(sessionUser.getId(), savedTeam.getId());
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teams/create";
        }
    }
    // REJOINDRE UNE ÉQUIPE (POST /teams/{id}/join)
    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session) {
        // 1. Vérifier si l'utilisateur est connecté
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // 2. Rafraîchir l'utilisateur depuis la BDD (sécurité)
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) return "redirect:/login";

        // 3. Vérifier qu'il n'a pas déjà une équipe
        if (user.getTeam() != null) {
            // Optionnel : Ajouter un message d'erreur "Vous avez déjà une équipe"
            return "redirect:/teams/my";
        }

        // 4. Récupérer l'équipe cible
        Team teamToJoin = teamService.findById(id);
        if (teamToJoin == null) return "redirect:/teams";

        // 5. Faire le lien (Rejoindre)
        // On met à jour l'utilisateur
        user.setTeam(teamToJoin);
        userRepository.save(user);

        // 6. Mettre à jour la session
        sessionUser.setTeam(teamToJoin);
        session.setAttribute("user", sessionUser);

        // 7. Rediriger vers "Mon Équipe"
        return "redirect:/teams/my";
    }
    // --- 3. INSCRIPTION TOURNOI ---
    @PostMapping("/register/{tournamentId}")
    public String registerToTournament(@PathVariable Long tournamentId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElseThrow();
        if (user.getTeam() == null) return "redirect:/teams/new";

        try {
            teamService.registerTeamToTournament(user.getTeam().getId(), tournamentId, user);
        } catch (RuntimeException e) {
            System.out.println("Erreur inscription : " + e.getMessage());
        }
        return "redirect:/teams/my";
    }

    // --- 4. GESTION DIVERS ---
    @PostMapping("/leave")
    public String leaveTeam(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userService.leaveTeam(user.getId());
            user.setTeam(null);
            session.setAttribute("user", user);
        }
        return "redirect:/";
    }

    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        User dbUser = userRepository.findById(user.getId()).orElseThrow();
        if (dbUser.getTeam() != null && dbUser.getTeam().getLeader().getId().equals(dbUser.getId())) {
            teamService.dissolveTeam(dbUser.getTeam().getId());
            user.setTeam(null);
            session.setAttribute("user", user);
        }
        return "redirect:/";
    }

    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session) {
        // ... (Code identique à avant) ...
        User user = (User) session.getAttribute("user");
        if (user != null) {
            try { userService.kickMember(user.getId(), memberId); } catch (Exception e) {}
        }
        return "redirect:/teams/my";
    }

    @GetMapping("/invite/{code}")
    public String joinByInvite(@PathVariable String code, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            userService.joinTeamByInviteCode(user.getId(), code);
            Team t = teamService.findByInviteCode(code);
            user.setTeam(t);
            session.setAttribute("user", user);
            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            return "redirect:/teams?error=" + e.getMessage();
        }
    }
    // CATALOGUE DES ÉQUIPES (GET /teams)
    @GetMapping("") 
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAllTeams());
        return "teams/list";
    }
}