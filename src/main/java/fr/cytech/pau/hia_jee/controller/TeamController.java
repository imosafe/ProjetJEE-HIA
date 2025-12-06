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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;
    // --- 1. Afficher "Mon Équipe" ---
    @GetMapping("/my")
    public String myTeam(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        if (sessionUser.getTeam() != null) {
            // On recharge l'équipe avec les membres (EAGER fetch)
            Team freshTeam = teamService.findTeamWithMembers(sessionUser.getTeam().getId());
            model.addAttribute("team", freshTeam);
            List<Tournament> compatibleTournaments = tournamentRepository.findByGameAndStatus(
                    freshTeam.getGame(), 
                    StatusTournament.OUVERT
            );
            
            // On retire ceux où l'équipe est déjà inscrite
            compatibleTournaments.removeIf(t -> t.getTeams().contains(freshTeam));

            model.addAttribute("availableTournaments", compatibleTournaments);
            return "teams/my-team";
        } else {
            return "redirect:/teams/new";
        }
    }

    // --- 2. Formulaire de Création ---
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (user.getTeam() != null) return "redirect:/teams/my";

        model.addAttribute("team", new Team());
        return "teams/create";
    }

    // --- 3. Traitement Création ---
    @PostMapping("/new")
    public String processCreate(@ModelAttribute Team team, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            // Création avec définition du LEADER
            Team savedTeam = teamService.createTeam(team, sessionUser);

            // Le user rejoint l'équipe
            userService.joinTeam(sessionUser.getId(), savedTeam.getId());

            // Mise à jour Session
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);

            return "redirect:/teams/my";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "teams/create";
        }
    }

    // --- 4. Quitter l'équipe ---
    @PostMapping("/leave")
    public String leaveTeam(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null && sessionUser.getTeam() != null) {
            userService.leaveTeam(sessionUser.getId());
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);
        }
        return "redirect:/";
    }

    // --- 5. Catalogue des Équipes (GET) ---
    @GetMapping({"", "/"})
    public String listTeams(Model model) {
        // On envoie la liste de TOUTES les équipes à la vue
        model.addAttribute("teams", teamService.findAllTeams());
        return "teams/list";
    }

    // --- 6. Rejoindre une Équipe (POST) ---
    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");

        // Sécurité : Faut être connecté
        if (user == null) return "redirect:/login";

        try {
            // Appel au service pour faire le lien en base de données
            userService.joinTeam(user.getId(), id);

            // MISE À JOUR DE LA SESSION (Crucial pour l'affichage immédiat)
            // On récupère l'équipe fraîchement rejointe
            Team joinedTeam = teamService.findTeamWithMembers(id);
            user.setTeam(joinedTeam);
            session.setAttribute("user", user);

            // Succès -> On redirige vers la page "Mon Équipe"
            return "redirect:/teams/my";

        } catch (RuntimeException e) {
            // Erreur (ex: déjà dans une team) -> Retour à la liste avec message
            return "redirect:/teams?error=" + e.getMessage();
        }
    }

    // --- 7. DISSOUDRE L'ÉQUIPE (Nouveau) ---
    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session) {
        User user = (User) session.getAttribute("user");

        // Vérif sécurité de base
        if (user == null || user.getTeam() == null) return "redirect:/login";

        // On vérifie que c'est bien le chef via la BDD
        Team team = teamService.findById(user.getTeam().getId());

        // Si l'équipe a un chef et que ce n'est PAS l'utilisateur courant -> Erreur
        if (team.getLeader() != null && !team.getLeader().getId().equals(user.getId())) {
            return "redirect:/teams/my"; // Ou page d'erreur
        }

        // Dissolution
        teamService.dissolveTeam(team.getId());

        // Mise à jour session
        user.setTeam(null);
        session.setAttribute("user", user);

        return "redirect:/";
    }

    // --- 8. Exclure un membre (KICK) ---
    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");

        // Sécurité de base
        if (sessionUser == null) return "redirect:/login";

        try {
            // On appelle le service avec (ID du chef, ID du membre à virer)
            userService.kickMember(sessionUser.getId(), memberId);

            // Si ça marche, on recharge la page
            return "redirect:/teams/my?success=Joueur exclu";

        } catch (RuntimeException e) {
            // Si erreur (ex: hack, pas le chef...), on affiche l'erreur
            return "redirect:/teams/my?error=" + e.getMessage();
        }
    }

    // --- 9. ROUTE D'INVITATION ---
    @GetMapping("/invite/{code}")
    public String joinByInvite(@PathVariable String code, HttpSession session) {
        User user = (User) session.getAttribute("user");

        // 1. Si pas connecté -> Login
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // 2. Le service fait le lien User <-> Team
            userService.joinTeamByInviteCode(user.getId(), code);

            // 3. IMPORTANT : Mise à jour de la session
            // On récupère l'équipe pour mettre à jour l'objet User stocké en mémoire
            Team joinedTeam = teamService.findByInviteCode(code);

            user.setTeam(joinedTeam);
            session.setAttribute("user", user);

            return "redirect:/teams/my?success=Bienvenue dans l'équipe !";

        } catch (RuntimeException e) {
            // Si erreur (déjà en équipe, code faux...)
            return "redirect:/teams?error=" + e.getMessage();
        }
    }
     // Inscrire l'équipe à un tournoi
     @PostMapping("/register/{tournamentId}")
    public String registerTeamToTournament(@PathVariable Long tournamentId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Refresh user
        User user = userRepository.findById(sessionUser.getId()).orElseThrow();
        Team team = user.getTeam();

        if (team == null) return "redirect:/teams/new";

        try {
            // Appel au Service qui contient toute la logique métier
            teamService.registerTeamToTournament(team.getId(), tournamentId, user);
        } catch (RuntimeException e) {
            // Pour faire simple, on log l'erreur console (ou on pourrait passer un param ?error)
            System.out.println("Erreur inscription tournoi : " + e.getMessage());
        }

        return "redirect:/teams/my";
    }
}