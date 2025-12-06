package fr.cytech.pau.hia_jee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.service.TeamService;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    // --- 1. AFFICHER LE PROFIL D'UNE ÉQUIPE (PUBLIC) ---
    // C'est la méthode la plus importante pour ta demande !
    @GetMapping("/{id}")
    public String showTeamProfile(@PathVariable Long id, Model model, HttpSession session) {
        Team team = teamService.findTeamWithMembers(id);
        if (team == null) return "redirect:/teams";

        User currentUser = (User) session.getAttribute("user");
        
        boolean isMember = false;
        boolean canJoin = false;
        boolean isLeader = false;

        if (currentUser != null) {
            if (currentUser.getTeam() != null && currentUser.getTeam().getId().equals(team.getId())) {
                isMember = true;
                if (team.getLeader() != null && team.getLeader().getId().equals(currentUser.getId())) {
                    isLeader = true;
                }
            } else if (currentUser.getTeam() == null) {
                canJoin = true;
            }
        }

        model.addAttribute("team", team);
        model.addAttribute("isMember", isMember);
        model.addAttribute("canJoin", canJoin);
        model.addAttribute("isLeader", isLeader);

        return "teams/profile"; 
    }

    // --- 2. REDIRECTION INTELLIGENTE "MON ÉQUIPE" ---
    @GetMapping("/my")
    public String myTeamRedirect(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getTeam() != null) {
            return "redirect:/teams/" + user.getTeam().getId();
        }
        return "redirect:/teams/new"; 
    }

    // --- 3. CATALOGUE ---
    @GetMapping({"", "/"})
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAllTeams());
        return "teams/list";
    }

    // --- 4. REJOINDRE ---
    @PostMapping("/{id}/join")
    public String joinTeam(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            userService.joinTeam(user.getId(), id);
            
            Team joinedTeam = teamService.findTeamWithMembers(id);
            user.setTeam(joinedTeam);
            session.setAttribute("user", user);

            return "redirect:/teams/" + id + "?success=Bienvenue !";
        } catch (RuntimeException e) {
            return "redirect:/teams/" + id + "?error=" + e.getMessage();
        }
    }

    // --- 5. QUITTER ---
    @PostMapping("/leave")
    public String leaveTeam(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getTeam() != null) {
            Long oldTeamId = user.getTeam().getId();
            userService.leaveTeam(user.getId());
            
            user.setTeam(null);
            session.setAttribute("user", user);
            
            return "redirect:/teams/" + oldTeamId + "?success=Vous avez quitté l'équipe.";
        }
        return "redirect:/";
    }

    // --- 6. CRÉATION ---
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("team", new Team());
        return "teams/create";
    }

    @PostMapping("/new")
    public String processCreate(@ModelAttribute Team team, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Team savedTeam = teamService.createTeam(team, user);
        userService.joinTeam(user.getId(), savedTeam.getId());
        
        user.setTeam(savedTeam);
        session.setAttribute("user", user);

        return "redirect:/teams/" + savedTeam.getId();
    }

    // --- 7. DISSOUDRE ---
    @PostMapping("/dissolve")
    public String dissolveTeam(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getTeam() != null) {
            teamService.dissolveTeam(user.getTeam().getId());
            user.setTeam(null);
            session.setAttribute("user", user);
        }
        return "redirect:/teams";
    }

    // --- 8. EXCLURE (KICK) ---
    @PostMapping("/kick/{memberId}")
    public String kickMember(@PathVariable Long memberId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if(user == null || user.getTeam() == null) return "redirect:/login";

        try {
            userService.kickMember(user.getId(), memberId);
            return "redirect:/teams/" + user.getTeam().getId() + "?success=Joueur exclu.";
        } catch (RuntimeException e) {
            return "redirect:/teams/" + user.getTeam().getId() + "?error=" + e.getMessage();
        }
    }

    // --- 9. INVITATION ---
    @GetMapping("/invite/{code}")
    public String joinByInvite(@PathVariable String code, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // 2. Le service fait le lien User <-> Team
            userService.joinTeamByInviteCode(user.getId(), code);
            Team joinedTeam = teamService.findByInviteCode(code);
            
            user.setTeam(joinedTeam);
            session.setAttribute("user", user);

            return "redirect:/teams/" + joinedTeam.getId() + "?success=Bienvenue !";
        } catch (RuntimeException e) {
            return "redirect:/teams?error=" + e.getMessage();
        }
    }
}