package fr.cytech.pau.hia_jee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;

//Contrôleur gérant la page de "Paramètres" (Settings) de l'utilisateur.

@Controller
@RequestMapping("/setting") 
public class SettingController {

    @Autowired
    private UserService userService;

    // ============================================================
    // AFFICHER LA PAGE DES PARAMÈTRES
    // ============================================================

    @GetMapping
    public String showSettings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        // Sécurité manuelle : Si l'utilisateur n'est pas connecté, on le renvoie au login.
        if (user == null) return "redirect:/login"; 

        // On passe l'utilisateur au modèle pour pré-remplir les champs (pseudo, etc.)
        model.addAttribute("user", user);
        return "setting"; // Vue: src/main/resources/templates/setting.html
    }

    // ============================================================
    // ACTION : CHANGER PSEUDO
    // ============================================================

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String username, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            // 1. Appel au service pour mise à jour en base de données
            User updatedUser = userService.updateUsername(sessionUser.getId(), username);
            
            // 2. MISE À JOUR DE LA SESSION (Crucial !)
            // L'objet "user" en session est une copie. Si on change la BDD mais pas la session,
            // le nom affiché dans la barre de navigation restera l'ancien jusqu'à la prochaine reconnexion.
            session.setAttribute("user", updatedUser);
            
            // 3. Message flash (s'affichera une seule fois après la redirection)
            redirectAttributes.addFlashAttribute("success", "Pseudo mis à jour avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        // On redirige vers la page GET pour éviter de renvoyer le formulaire si on actualise (F5)
        return "redirect:/setting";
    }

    // ============================================================
    // ACTION : CHANGER MOT DE PASSE
    // ============================================================

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword,
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Le service gère toute la logique complexe (vérification ancien mdp, hachage nouveau mdp, etc.)
            userService.updatePassword(user.getId(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/setting";
    }

    // ============================================================
    // ACTION : QUITTER EQUIPE
    // ============================================================

    @PostMapping("/leave-team")
    public String leaveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // 1. Mise à jour en Base de Données
            userService.leaveTeam(user.getId());
            
            // 2. Mise à jour de la Session
            // On doit manuellement dire à l'objet en session qu'il n'a plus d'équipe,
            // sinon l'interface affichera encore "Mon Équipe" au lieu de disparaître.
            user.setTeam(null);
            session.setAttribute("user", user);
            
            redirectAttributes.addFlashAttribute("success", "Vous avez quitté l'équipe.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/setting";
    }

    // ============================================================
    // ACTION : SUPPRIMER COMPTE
    // ============================================================

   
    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            // Suppression en BDD
            userService.deleteAccount(user.getId());
            
            // Destruction de la session (déconnexion immédiate)
            session.invalidate(); 
        }
        return "redirect:/"; // Retour à l'accueil publique
    }
}