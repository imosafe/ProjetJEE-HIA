package fr.cytech.pau.hia_jee.controller; 

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//Contrôleur principal gérant la page d'accueil .
 
@Controller
public class HomeController {

    //Gère les requêtes sur la racine du site (http://localhost:8080/).

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        
        // 1. Récupération de l'utilisateur en session.
        // `session.getAttribute("user")` renvoie un Object (ou null si personne n'est connecté).
        Object user = session.getAttribute("user");

        // 2. Transmission à la Vue.
        // On injecte cet objet (User ou null) dans le modèle sous la clé "user".
        // Cela permettra à Thymeleaf (dans index.html) de faire des affichages conditionnels.
        // Exemple : <div th:if="${user != null}">Bonjour, Admin!</div>
        model.addAttribute("user", user);

        // 3. Renvoi de la vue.
        // Spring va chercher le fichier src/main/resources/templates/index.html
        return "index"; 
    }
}