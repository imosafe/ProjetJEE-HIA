package fr.cytech.pau.hia_jee.controller; // Vérifie ton package !

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // On récupère l'utilisateur en session (s'il existe)
        Object user = session.getAttribute("user");

        // On le passe à la vue pour l'affichage dynamique
        model.addAttribute("user", user);

        return "index"; // Cherche le fichier templates/index.html
    }
}