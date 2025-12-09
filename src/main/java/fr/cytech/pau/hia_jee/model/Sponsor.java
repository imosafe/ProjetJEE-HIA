package fr.cytech.pau.hia_jee.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated; 
import jakarta.persistence.GeneratedValue; 
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

/**
 * Entité représentant un Partenaire / Sponsor.
 * Un sponsor peut financer plusieurs tournois, et un tournoi peut avoir plusieurs sponsors.
 */
@Entity 
public class Sponsor {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String logoUrl;     // Lien vers l'image du logo
    private String websiteUrl;  // Lien vers le site vitrine du sponsor

    // --- GESTION DES ENUMS EN BASE DE DONNÉES ---
    
    /**
     * Type de sponsor (ex: TECHNIQUE, FINANCIER, MEDIA).
     */
    @Enumerated(EnumType.STRING) 
    private SponsorType type;

    /**
     * Niveau de sponsoring (ex: GOLD, SILVER, BRONZE).
     */
    @Enumerated(EnumType.STRING) 
    private SponsorshipLevel level;
    /* 
    * * 'mappedBy = "Sponsors"' indique que c'est l'AUTRE classe (Tournament) qui est propriétaire de la relation.
     * * Cela signifie :
     * 1. Dans la table de jointure (ex: tournament_sponsors), c'est le Tournament qui pilote les insertions.
     * 2. Le champ "Sponsors" (avec majuscule ou minuscule selon votre classe Tournament) doit exister dans Tournament.
     * * Utilisation d'un Set (et non List) pour éviter d'avoir deux fois le même tournoi.
     * */
    @ManyToMany(mappedBy = "Sponsors") 
    private Set<Tournament> tournaments = new HashSet<>(); 

    // --- CONSTRUCTEURS ---

    public Sponsor() {}
    
    public Sponsor(String name) {
        this.name = name;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public SponsorType getType() {
        return type;
    }
    public void setType(SponsorType type) {
        this.type = type;
    }

    public SponsorshipLevel getLevel() {
        return level;
    }
    public void setLevel(SponsorshipLevel level) {
        this.level = level;
    }
    
    public Set<Tournament> getTournaments() {
       return tournaments;
    }
    
    // Pas de Setter pour tournaments généralement dans le "mappedBy", 
    // ou alors il faut gérer la synchronisation des deux côtés.
}