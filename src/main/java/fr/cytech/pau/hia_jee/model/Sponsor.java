package fr.cytech.pau.hia_jee.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated; // Import nécessaire pour @ManyToMany
import jakarta.persistence.GeneratedValue; // Import nécessaire pour Set
import jakarta.persistence.GenerationType; // Import nécessaire pour HashSet
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity 
public class Sponsor {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String logoUrl;
    private String websiteUrl;

    @Enumerated(EnumType.STRING) // OBLIGATOIRE pour la sécurité , il mest string pas numeration de 0 a 3 
    private SponsorType type;

    @Enumerated(EnumType.STRING) // OBLIGATOIRE pour la sécurité , il mest string pas numeration de 0 a 3 
    private SponsorshipLevel level;




    
    // Relation ManyToMany
    @ManyToMany(mappedBy = "Sponsors") 
    private Set<Tournament> tournaments = new HashSet<>(); 

    public Sponsor() {}
    public Sponsor(String name) {
        this.name = name;
        
    }

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

   //liste des tournois associés à un sponsor
    public Set<Tournament> getTournaments() {
       return tournaments;
    }

}