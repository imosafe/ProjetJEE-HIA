package fr.cytech.pau.hia_jee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LadderController {

    @GetMapping("/ladder")
    public String showLadder() {
        return "ladder"; 
    }
}