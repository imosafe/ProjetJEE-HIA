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

// Contrôleur gérant tout le flux d'authentification :

@Controller
public class AuthController {

    // Injection du service métier qui contient la logique de BDD (UserRepository)
    @Autowired
    private UserService userService;

    // ============================================================
    // --- REGISTER (Inscription) ---
    // ============================================================

    /**
     * Affiche le formulaire d'inscription.
     * URL : GET /register
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // On passe un objet User vide au formulaire pour que Thymeleaf puisse
        // faire le lien (binding) avec les champs (th:object="${user}").
        model.addAttribute("user", new User());
        return "auth/register"; // Renvoie le fichier src/main/resources/templates/auth/register.html
    }

    /**
     * Traite les données soumises par le formulaire d'inscription.
     * URL : POST /register
     *
     * @param user  L'objet User rempli automatiquement par Spring avec les données du formulaire.
     * @param model Le modèle pour renvoyer des erreurs si besoin.
     */
    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {
        try {
            // Appel au service pour sauvegarder l'utilisateur.
            // C'est le service qui définit le rôle par défaut (PLAYER).
            userService.register(user);
            
            // Si tout se passe bien, on REDIRIGE vers la page de login.
            // Le "?success" permet d'afficher un bandeau vert sur la page de login.
            return "redirect:/login?success"; 
        } catch (RuntimeException e) {
            // En cas d'erreur (ex: le nom d'utilisateur existe déjà),
            // on attrape l'exception lancée par le Service.
            
            // On ajoute le message d'erreur au modèle pour l'afficher dans la vue.
            model.addAttribute("error", e.getMessage());
            
            // On retourne la MEME page d'inscription pour que l'utilisateur corrige.
            return "auth/register";
        }
    }

    // ============================================================
    // --- LOGIN (Connexion) ---
    // ============================================================

    /**
     * Affiche le formulaire de connexion.
     * URL : GET /login
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login"; 
    }

    /**
     * Traite la tentative de connexion.
     * URL : POST /login
     *
     * @param username Le pseudo saisi.
     * @param password Le mot de passe saisi.
     * @param session  L'objet HttpSession géré par le serveur (Tomcat). C'est ici qu'on stocke l'état connecté.
     * @param model    Pour afficher les erreurs.
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // Vérification des identifiants via le service
        User user = userService.authenticate(username, password);

        if (user != null) {
            // --- SUCCÈS ---
            
            // 1. Mise en session : C'est l'étape CRUCIALE.
            // Tant que cet objet "user" est dans la session, l'utilisateur est considéré comme connecté.
            session.setAttribute("user", user);

            // 2. Redirection conditionnelle selon le Rôle
            if (user.getRole() == Role.ADMIN) {
                // Si c'est un admin, on pourrait le rediriger vers /admin/dashboard
                // Pour l'instant, redirection vers l'accueil.
                return "redirect:/";
            } else {
                // Si c'est un joueur lambda, redirection vers l'accueil public.
                return "redirect:/";
            }
        } else {
            // --- ÉCHEC ---
            // On reste sur la page de login et on affiche une erreur générique de sécurité.
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect.");
            return "auth/login";
        }
    }

    // ============================================================
    // --- LOGOUT (Déconnexion) ---
    // ============================================================

    /**
     * Gère la déconnexion.
     * URL : GET /logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalider la session détruit toutes les données stockées (l'objet "user" disparait).
        // Au prochain chargement de page, le serveur considérera l'utilisateur comme un nouveau visiteur anonyme.
        session.invalidate(); 
        
        // Redirection vers la page de login
        return "redirect:/login";
    }
}