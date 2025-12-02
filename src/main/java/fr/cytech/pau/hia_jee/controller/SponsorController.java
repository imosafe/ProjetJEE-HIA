package fr.cytech.pau.hia_jee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cytech.pau.hia_jee.model.Sponsor;
import fr.cytech.pau.hia_jee.service.SponsorService;
import fr.cytech.pau.hia_jee.repository.SponsorRepository; // <-- Import du Repository pour la recherche

@Controller 
@RequestMapping("/admin/sponsors") 
public class SponsorController {

    @Autowired
    private SponsorService sponsorService; // On garde ton service pour save/delete

    @Autowired
    private SponsorRepository sponsorRepository; // <-- On ajoute ça pour la méthode 'search'

    // 1. Affiche la liste AVEC Pagination et Recherche
    @GetMapping 
    public String listSponsors(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,      // Page 0 par défaut
            @RequestParam(name = "keyword", defaultValue = "") String keyword // Vide par défaut
    ) {
        // On crée la demande de page (page actuelle, 6 éléments max)
        PageRequest pageable = PageRequest.of(page, 6);

        // On appelle la méthode 'search' du Repository
        Page<Sponsor> pageSponsors = sponsorRepository.search(keyword, pageable);

        // On envoie le résultat (Page) à la vue
        model.addAttribute("sponsors", pageSponsors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);

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