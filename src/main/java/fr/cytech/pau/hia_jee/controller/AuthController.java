package fr.cytech.pau.hia_jee.controller;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // --- REGISTER (Inscription) ---

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register"; // Assure-toi que le fichier est dans templates/auth/register.html
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {
        try {
            // Le service va se charger de mettre le Role.PLAYER
            userService.register(user);
            return "redirect:/login?success"; // Renvoie vers le login avec message vert
        } catch (RuntimeException e) {
            // En cas d'erreur (pseudo pris), on recharge la page avec l'erreur
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // --- LOGIN (Connexion) ---

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; // Assure-toi que le fichier est dans templates/auth/login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        User user = userService.authenticate(username, password);

        if (user != null) {
            // 1. On stocke l'objet User complet en session
            session.setAttribute("user", user);

            // 2. Redirection intelligente selon le Rôle (Enum)
            if (user.getRole() == Role.ADMIN) {
                // Les admins vont vers leur dashboard (à créer plus tard par Dev A ou C)
                // Pour l'instant, redirigeons vers l'accueil pour éviter une 404
                return "redirect:/";
            } else {
                // Les joueurs vont vers l'accueil
                return "redirect:/";
            }
        } else {
            // Login échoué
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect.");
            return "auth/login";
        }
    }

    // --- LOGOUT (Déconnexion) ---

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Détruit la session (l'utilisateur est oublié)
        return "redirect:/login";
    }
}