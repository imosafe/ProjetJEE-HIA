package fr.cytech.pau.hia_jee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//Contrôleur gérant l'affichage de la page du classement.
 
@Controller
public class LadderController {

    /**
     * Gère la requête pour voir le classement.
     * URL : GET /ladder
     *
     * @return Le nom logique de la vue ("ladder"), que Spring va résoudre 
     * en cherchant le fichier "src/main/resources/templates/ladder.html".
     */
    @GetMapping("/ladder")
    public String showLadder() {
        // On renvoie directement la vue.
        return "ladder"; 
    }
}