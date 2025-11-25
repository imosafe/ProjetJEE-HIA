
package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class Sponsor {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    // Relation ManyToMany (côté passif)
    //@ManyToMany(mappedBy = "sponsors") 
    //private Set<Tournament> tournaments = new HashSet<>(); 

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
    //public Set<Tournament> getTournaments() {
      //  return tournaments;
    //}

}