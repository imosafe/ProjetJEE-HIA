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

@Controller
@RequestMapping("/setting")
public class SettingController {

    @Autowired
    private UserService userService;

    // AFFICHER LA PAGE
    @GetMapping
    public String showSettings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        // Sécurité : Si pas connecté, on renvoie au login
        if (user == null) return "redirect:/login"; 

        model.addAttribute("user", user);
        return "setting"; // Correspond à setting.html
    }

    // ACTION : CHANGER PSEUDO
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String username, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        try {
            // Appel au service
            User updatedUser = userService.updateUsername(sessionUser.getId(), username);
            
            // IMPORTANT : Mettre à jour l'utilisateur en session pour que le header change tout de suite
            session.setAttribute("user", updatedUser);
            
            redirectAttributes.addFlashAttribute("success", "Pseudo mis à jour avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/setting";
    }

    // ACTION : CHANGER MOT DE PASSE
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword,
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            userService.updatePassword(user.getId(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/setting";
    }

    // ACTION : QUITTER EQUIPE
    @PostMapping("/leave-team")
    public String leaveTeam(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // On utilise ta méthode existante leaveTeam(Long userId)
            userService.leaveTeam(user.getId());
            
            // On met à jour la session manuellement (retirer l'équipe de l'objet session)
            user.setTeam(null);
            session.setAttribute("user", user);
            
            redirectAttributes.addFlashAttribute("success", "Vous avez quitté l'équipe.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/setting";
    }

    // ACTION : SUPPRIMER COMPTE
    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userService.deleteAccount(user.getId());
            session.invalidate(); // Déconnexion forcée
        }
        return "redirect:/"; // Retour à l'accueil
    }
}