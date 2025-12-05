package fr.cytech.pau.hia_jee.controller;

import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.service.TeamService;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teams") // Toutes les URL commencent par /teams
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    // --- 1. Afficher "Mon Équipe" ---
    @GetMapping("/my")
    public String myTeam(HttpSession session, Model model) {
        // Sécurité : On vérifie si l'utilisateur est connecté
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // IMPORTANT : L'objet en session peut être "périmé".
        // On demande à la BDD la version la plus fraîche de l'utilisateur pour voir s'il a une équipe.
        // (Supposons que tu as ajouté findById dans UserService, sinon utilise le Repo direct)
        // Ici, pour faire simple, on fait confiance à la session mise à jour.

        if (sessionUser.getTeam() != null) {
            // L'utilisateur a une équipe, on l'envoie vers la page de profil
            model.addAttribute("team", sessionUser.getTeam());
            return "teams/my-team";
        } else {
            // L'utilisateur n'a pas d'équipe, on affiche la page qui propose de créer/rejoindre
            // (Tu peux créer un fichier no-team.html ou rediriger vers create)
            return "redirect:/teams/new";
        }
    }

    // --- 2. Formulaire de Création (GET) ---
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Si le joueur a déjà une équipe, on l'empêche d'en créer une autre (Règle Métier)
        if (user.getTeam() != null) {
            return "redirect:/teams/my";
        }

        // On envoie un objet Team vide au formulaire pour qu'il le remplisse
        model.addAttribute("team", new Team());
        return "teams/create"; // Cherche src/main/resources/templates/teams/create.html
    }

    // --- 3. Traitement de la Création (POST) ---
    @PostMapping("/new")
    public String processCreate(@ModelAttribute Team team, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            // A. On enregistre l'équipe en base (le champ 'game' est rempli auto par le formulaire)
            Team savedTeam = teamService.createTeam(team);

            // B. Le créateur rejoint l'équipe automatiquement (Relation User -> Team)
            userService.joinTeam(sessionUser.getId(), savedTeam.getId());

            // C. MISE À JOUR DE LA SESSION
            // C'est crucial : sinon l'utilisateur devra se reco pour voir son équipe
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);

            return "redirect:/teams/my"; // Succès !

        } catch (RuntimeException e) {
            // En cas d'erreur (ex: Nom d'équipe déjà pris)
            model.addAttribute("error", e.getMessage());
            return "teams/create"; // On recharge le formulaire avec l'erreur
        }
    }

    // --- 4. Quitter l'équipe (POST) ---
    @PostMapping("/leave")
    public String leaveTeam(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null && sessionUser.getTeam() != null) {

            // Logique métier pour quitter
            userService.leaveTeam(sessionUser.getId());

            // Mise à jour de la session (on retire l'équipe de l'objet en mémoire)
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);
        }
        return "redirect:/"; // Retour à l'accueil
    }

    // --- 5. Catalogue des Équipes (GET) ---
    @GetMapping("") // Mappe sur /teams (car la classe a @RequestMapping("/teams"))
    public String listTeams(Model model) {
        // On récupère toutes les équipes via le service
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
            // Appel au service (Dev B) pour faire le lien
            userService.joinTeam(user.getId(), id);

            // Mise à jour de la session (Important !)
            // On récupère l'équipe fraîchement rejointe pour mettre à jour l'objet User en mémoire
            Team joinedTeam = teamService.findById(id);
            user.setTeam(joinedTeam);
            session.setAttribute("user", user);

            // Succès -> On va vers "Mon Équipe"
            return "redirect:/teams/my";

        } catch (RuntimeException e) {
            // Erreur (ex: Équipe pleine, déjà dans une team...)
            // On redirige vers le catalogue avec un message d'erreur dans l'URL
            return "redirect:/teams?error=" + e.getMessage();
        }
    }
}