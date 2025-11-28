package fr.cytech.pau.hia_jee.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // Import nécessaire pour @ManyToMany
import jakarta.persistence.Id; // Import nécessaire pour Set
import jakarta.persistence.ManyToMany; // Import nécessaire pour HashSet

@Entity 
public class Sponsor {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
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


   //liste des tournois associés à un sponsor
    public Set<Tournament> getTournaments() {
       return tournaments;
    }

}