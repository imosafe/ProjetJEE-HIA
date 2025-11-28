package fr.cytech.pau.hia_jee.controller; // Le package de votre contrôleur est correct

// Les imports ont été corrigés pour  le package de base 'fr.cytech.pau.hia_jee'

import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.cytech.pau.hia_jee.model.Sponsor;
import fr.cytech.pau.hia_jee.service.SponsorService;

@Controller 
@RequestMapping("/admin/sponsors") 
public class SponsorController {

    @Autowired
    private SponsorService sponsorService;

    // 1. Affiche la liste des sponsors (GET: /admin/sponsors)
    @GetMapping 
    public String listSponsors(Model model) {
        List<Sponsor> sponsors = sponsorService.findAll();
        model.addAttribute("sponsors", sponsors);

        return "/admin/sponsors/list"; 
    }

    @GetMapping("/new")
    public String showSponsorForm(Model model) {
        model.addAttribute("sponsor", new Sponsor());
        return "/admin/sponsors/form"; 
    }

    @PostMapping
    public String saveSponsor(@ModelAttribute Sponsor sponsor) {
        sponsorService.save(sponsor); 

        return "redirect:/admin/sponsors"; 
    }

    @GetMapping("/delete/{id}")
    public String deleteSponsor(@PathVariable Long id) {
        sponsorService.deleteById(id);
        return "redirect:/admin/sponsors";
    }
}