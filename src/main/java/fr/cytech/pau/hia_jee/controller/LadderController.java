package fr.cytech.pau.hia_jee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//Contrôleur gérant l'affichage de la page du classement.
 
@Controller
public class LadderController {

    /**
     * Gère la requête pour voir le classement.
     */
    @GetMapping("/ladder")
    public String showLadder() {
        // On renvoie directement la vue.
        return "ladder"; 
    }
}