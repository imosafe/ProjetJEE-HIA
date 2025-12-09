package fr.cytech.pau.hia_jee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // IMPORTANT : Pour gérer l'erreur SQL
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

// Contrôleur principal pour la gestion des Équipes (Teams).

@Controller
@RequestMapping("/teams")
public class TeamController {

    // 🗣️ "J'utilise l'Injection de Dépendances (@Autowired) pour accéder à mes services et repositories sans couplage fort."
    @Autowired private TeamService teamService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;       // Utile pour rafraîchir les données User
    @Autowired private TournamentRepository tournamentRepository; // Utile pour lister les tournois dispos

    // ============================================================
    // 1. LISTE ET PROFIL PUBLIC
    // ============================================================

    @GetMapping("")
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAllTeams());
        return "teams/list";
    }

    /**
     * Affiche le profil public d'une équipe spécifique.
     * Gère la logique d'affichage des boutons (Rejoindre, Quitter) selon qui regarde la page.
     */
    @GetMapping("/{id}")
    public String showTeamProfile(@PathVariable Long id, Model model, HttpSession session) {
        // Chargement de l'équipe (avec ses membres si possible pour éviter le LazyInitializationException)
        Team team = teamService.findTeamWithMembers(id);
        if (team == null) return "redirect:/teams";

        User sessionUser = (User) session.getAttribute("user");

        // Drapeaux pour la vue (Thymeleaf)
        boolean isMember = false;
        boolean canJoin = false;
        boolean isLeader = false;

        if (sessionUser != null) {
            // IMPORTANT : On recharge l'utilisateur depuis la BDD via son ID.
            // Pourquoi ? L'objet en session ("sessionUser") peut être périmé (ex: il a rejoint une équipe
            // dans un autre onglet, mais la session n'est pas à jour).

            // 🗣️ "SÉCURITÉ SESSION : Je vérifie si l'utilisateur existe toujours en BDD."
            // "Si un admin l'a supprimé pendant qu'il naviguait, la session Java est obsolète. Je force la déconnexion."
            User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);

            if (dbUser != null) {
                // Si l'utilisateur a une équipe...
                if (dbUser.getTeam() != null) {
                    // ... et que c'est celle qu'on regarde actuellement
                    if (dbUser.getTeam().getId().equals(team.getId())) {
                        isMember = true;

                        // Vérification si c'est le chef (Leader)
                        if (team.getLeader() != null && team.getLeader().getId().equals(dbUser.getId())) {
                            isLeader = true;
                        }
                    }
                } else {
                    // Si l'utilisateur n'a AUCUNE équipe, il peut voir le bouton "Rejoindre"
                    canJoin = true;
                }
            }
        }

        // Envoi des infos à la vue
        model.addAttribute("team", team);
        model.addAttribute("isMember", isMember);
        model.addAttribute("canJoin", canJoin);
        model.addAttribute("isLeader", isLeader);

        return "teams/profile";
    }

    // ============================================================
    // 2. TABLEAU DE BORD "MON ÉQUIPE"
    // ============================================================

    /**
     * Page privée de gestion de sa propre équipe.
     * Redirige vers la création si l'utilisateur n'a pas d'équipe.
     * Affiche aussi les tournois compatibles.
     */
    @GetMapping("/my")
    public String myTeam(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Rafraîchissement des données utilisateur
        User user = userRepository.findById(sessionUser.getId()).orElse(null);

        // Si pas d'utilisateur ou pas d'équipe -> direction page de création
        if (user == null || user.getTeam() == null) return "redirect:/teams/new";

        // Récupération de l'équipe complète
        Team team = teamService.findTeamWithMembers(user.getTeam().getId());
        model.addAttribute("team", team);

        // Configuration des permissions (Ici, on est forcément membre)
        boolean isLeader = (team.getLeader() != null && team.getLeader().getId().equals(user.getId()));
        model.addAttribute("isMember", true);
        model.addAttribute("isLeader", isLeader);
        model.addAttribute("canJoin", false); // On ne peut pas rejoindre sa propre équipe

        // --- LOGIQUE DE FILTRAGE DES TOURNOIS ---
        // On veut proposer uniquement les tournois pertinents pour cette équipe.

        // 1. On récupère tous les tournois "OUVERT"
        List<Tournament> allUpcoming = tournamentRepository.findByStatus(StatusTournament.OUVERT);
        List<Tournament> compatibleTournaments = new ArrayList<>();

        if (allUpcoming != null) {

            // 🗣️ "LOGIQUE MÉTIER : Algorithme de filtrage."
            // "Je ne montre pas tous les tournois. Je filtre Java pour ne garder que ceux du même jeu (LoL vs Valorant)."
            for (Tournament t : allUpcoming) {
                // Condition 1 : Le jeu du tournoi doit correspondre au jeu de l'équipe
                boolean sameGame = (team.getGame() != null && team.getGame().equals(t.getGame()));

                // Condition 2 : L'équipe ne doit pas DÉJÀ être inscrite
                // (On parcourt la liste des équipes inscrites au tournoi pour vérifier)
                boolean notRegistered = t.getTeams().stream()
                        .noneMatch(registeredTeam -> registeredTeam.getId().equals(team.getId()));

                if (sameGame && notRegistered) {
                    compatibleTournaments.add(t);
                }
            }
        }

        model.addAttribute("availableTournaments", compatibleTournaments);

        // On réutilise la vue 'profile' car elle est conçue pour s'adapter
        return "teams/profile";
    }

    // ============================================================
    // 3. CRÉATION D'ÉQUIPE
    // ============================================================

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // CORRECTION : Au lieu de orElseThrow(), on gère le cas où l'user n'existe plus
        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);

        if (dbUser == null) {
            // L'utilisateur en session a été supprimé de la BDD => On le déconnecte proprement
            session.invalidate();
            return "redirect:/login";
        }

        // Si l'utilisateur a déjà une équipe, il ne peut pas en créer une autre
        if (dbUser.getTeam() != null) return "redirect:/teams/my";

        model.addAttribute("team", new Team());
        return "teams/create";
    }

    @PostMapping("/new")
    public String processCreate(@ModelAttribute Team team, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Double sécurité : on vérifie manuellement avant le try/catch
        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser != null && dbUser.getTeam() != null) {
            model.addAttribute("error", "Vous appartenez déjà à une équipe. Impossible d'en créer une nouvelle.");
            return "teams/create";
        }

        try {
            // Le service crée l'équipe et assigne l'utilisateur comme Leader
            Team savedTeam = teamService.createTeam(team, sessionUser);

            // CORRECTION: On lie explicitement l'utilisateur à l'équipe en base de données
            // C'est vital pour que le lien soit persistant
            if (dbUser != null) {
                dbUser.setTeam(savedTeam);
                userRepository.save(dbUser);
            }

            // Mise à jour de la session
            sessionUser.setTeam(savedTeam);
            session.setAttribute("user", sessionUser);

            return "redirect:/teams/my";

        } catch (DataIntegrityViolationException e) {
            // 🗣️ "ROBUSTESSE : J'intercepte ici l'erreur SQL (Contrainte UNIQUE)."
            // "Si l'utilisateur essaie de tricher et de créer 2 équipes, la BDD renvoie une erreur."
            // "Je la transforme en message lisible pour ne pas crasher l'application (Erreur 500)."
            model.addAttribute("error", "Impossible de créer l'équipe : Vous êtes déjà chef d'une équipe ou ce nom est déjà pris.");
            return "teams/create";

        } catch (RuntimeException e) {
            // Autres erreurs génériques
            model.addAttribute("error", e.getMessage());
            return "teams/create";
        }
    }

    // ============================================================
    // 4. ACTIONS (Rejoindre, Quitter, Dissoudre...)
    // ============================================================

    /**
     * Action pour rejoindre une équipe via le bouton "Rejoindre".
     */
    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        // Vérification en BDD
        User user = userRepository.findById(sessionUser.getId()).orElse(null);

        if (user == null || user.getTeam() != null) {
            redirectAttributes.addFlashAttribute("error", "Impossible : Vous avez déjà une équipe.");
            return "redirect:/teams/my";
        }

        try {
            Team teamToJoin = teamService.findById(id);
            if (teamToJoin != null) {
                // Logique métier d'adhésion
                user.setTeam(teamToJoin);
                userRepository.save(user);

                // Mise à jour Session
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

    /**
     * Inscrit l'équipe de l'utilisateur connecté à un tournoi.
     */
    @PostMapping("/register/{tournamentId}")
    public String registerToTournament(@PathVariable Long tournamentId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null || user.getTeam() == null) return "redirect:/teams/new";

        try {
            // Appel au service qui gère la vérification (places dispos, statut tournoi, etc.)
            teamService.registerTeamToTournament(user.getTeam().getId(), tournamentId, user);
            redirectAttributes.addFlashAttribute("success", "Inscription confirmée !");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Échec inscription : " + e.getMessage());
        }
        return "redirect:/teams/my";
    }

    /**
     * Permet à un membre simple de quitter l'équipe.
     */
    @PostMapping("/leave")
    public String leaveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            try {
                // Appel Service
                userService.leaveTeam(sessionUser.getId());

                // Mise à jour Session : On retire l'équipe de l'objet en mémoire
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

    /**
     * Permet au LEADER de dissoudre l'équipe (supprimer l'équipe pour tout le monde).
     */
    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // Vérification stricte : Seul le leader peut dissoudre
        if (dbUser.getTeam() != null && dbUser.getTeam().getLeader() != null
                && dbUser.getTeam().getLeader().getId().equals(dbUser.getId())) {

            Long teamId = dbUser.getTeam().getId(); // On sauvegarde l'ID

            // --- CORRECTIF DE SÉCURITÉ CONTRE LA CASCADE ---
            // On retire d'abord l'utilisateur de l'équipe et on SAUVEGARDE l'utilisateur.
            // Cela tente de briser le lien côté User avant que l'équipe ne soit supprimée.

            // 🗣️ "GESTION DES CASCADES JPA : Suppression complexe."
            // "Je dois rompre le lien User <-> Team manuellement avant de supprimer l'équipe."
            // "Sinon, selon la config Hibernate, supprimer l'équipe pourrait supprimer l'utilisateur (CascadeType.ALL)."
            dbUser.setTeam(null);
            userRepository.save(dbUser); // 🗣️ "Le .save() est obligatoire pour valider l'UPDATE SQL."
            // Mise à jour de la session immédiatement
            sessionUser.setTeam(null);
            session.setAttribute("user", sessionUser);

            // Ensuite, on supprime l'équipe
            teamService.dissolveTeam(teamId);

            redirectAttributes.addFlashAttribute("success", "L'équipe a été dissoute.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Action non autorisée (Vous n'êtes pas le chef).");
            return "redirect:/teams/my";
        }
        return "redirect:/teams";
    }

    /**
     * Permet au LEADER d'exclure un membre.
     */
    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            try {
                // La méthode kickMember du service doit vérifier que c'est bien le leader qui demande
                userService.kickMember(sessionUser.getId(), memberId);
                redirectAttributes.addFlashAttribute("success", "Membre exclu avec succès.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:/teams/my";
    }

    /**
     * Route spéciale pour rejoindre via un lien d'invitation.
     */
    @GetMapping("/invite/{code}")
    public String joinByInvite(@PathVariable String code, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            userService.joinTeamByInviteCode(sessionUser.getId(), code);
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