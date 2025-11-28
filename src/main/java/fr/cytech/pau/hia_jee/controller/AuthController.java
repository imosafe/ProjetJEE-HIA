package fr.cytech.pau.hia_jee.controller;


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
        model.addAttribute("user", new User()); // Objet vide pour le formulaire Thymeleaf
        return "auth/register"; // Renvoie vers templates/auth/register.html
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {
        try {
            // Par défaut, tout nouvel inscrit est un PLAYER (sécurité)
            user.setRole("PLAYER");
            userService.register(user);
            return "redirect:/login?success"; // Redirection vers login après succès
        } catch (RuntimeException e) {
            // En cas d'erreur (ex: pseudo déjà pris), on recharge la page avec le message
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // --- LOGIN (Connexion) ---

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; // Renvoie vers templates/auth/login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // Appel au service créé précédemment
        User user = userService.authenticate(username, password);

        if (user != null) {
            // C'est ICI que la magie de la sécurité manuelle opère
            // On stocke l'objet user entier dans la session du serveur
            session.setAttribute("user", user);

            // Redirection selon le rôle
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/"; // Accueil pour les joueurs
            }
        } else {
            model.addAttribute("error", "Invalid username or password");
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