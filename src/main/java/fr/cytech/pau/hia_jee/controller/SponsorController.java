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
import fr.cytech.pau.hia_jee.repository.SponsorRepository; 

//Contrôleur gérant la gestion des Sponsors dans la partie Administration.

@Controller 
@RequestMapping("/admin/sponsors") // Toutes les routes de ce fichier commencent par /admin/sponsors
public class SponsorController {

    @Autowired
    private SponsorService sponsorService; 

    // Injection directe du repository ici pour utiliser les fonctions de pagination avancées de Spring Data JPA.
    // (Note : Idéalement, la logique de recherche devrait être encapsulée dans le Service, 
    // mais l'appel direct au Repository est fréquent pour des recherches simples).
    @Autowired
    private SponsorRepository sponsorRepository; 

    // ============================================================
    // 1. LISTE AVEC PAGINATION ET RECHERCHE
    // ============================================================

    @GetMapping
    public String listSponsors(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,      
            @RequestParam(name = "keyword", defaultValue = "") String keyword 
    ) {
        // 1. Configuration de la pagination
        // On demande à Spring de préparer une "page" contenant 6 éléments maximum.
        PageRequest pageable = PageRequest.of(page, 6);

        // 2. Récupération des données
        // L'objet 'Page<Sponsor>' est très puissant : il contient la liste des sponsors
        // MAIS AUSSI des métadonnées (nombre total de pages, page actuelle, y a-t-il une page suivante ?).
        Page<Sponsor> pageSponsors = sponsorRepository.search(keyword, pageable);

        // 3. Envoi à la vue
        model.addAttribute("sponsors", pageSponsors); // L'objet Page complet
        model.addAttribute("keyword", keyword);       // Pour réafficher le mot cherché dans la barre
        model.addAttribute("currentPage", page);      // Pour mettre en surbrillance le bouton de page actuel

        return "/admin/sponsors/list"; // Vue: src/main/resources/templates/admin/sponsors/list.html
    }

    // ============================================================
    // 2. FORMULAIRE DE CRÉATION
    // ============================================================

    @GetMapping("/new")
    public String showSponsorForm(Model model) {
        // On injecte un objet vide pour que le formulaire HTML puisse s'y attacher (th:object="${sponsor}")
        model.addAttribute("sponsor", new Sponsor());
        return "/admin/sponsors/form"; 
    }

    // ============================================================
    // 3. ENREGISTREMENT (CREATE / UPDATE)
    // ============================================================

    @PostMapping
    public String saveSponsor(@ModelAttribute Sponsor sponsor) {
        // Le service s'occupe de sauvegarder (INSERT ou UPDATE selon si l'ID existe déjà)
        sponsorService.save(sponsor); 
        
        // Redirection vers la liste pour voir le résultat
        return "redirect:/admin/sponsors"; 
    }

    // ============================================================
    // 4. SUPPRESSION
    // ============================================================

    @GetMapping("/delete/{id}")
    public String deleteSponsor(@PathVariable Long id) {
        sponsorService.deleteById(id); 
        
        // On redirige vers la liste après suppression
        return "redirect:/admin/sponsors";
    } 
}