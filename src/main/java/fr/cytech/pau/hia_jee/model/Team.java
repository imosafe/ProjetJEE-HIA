// DANS Team.java
package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data; 

@Data // <<< AJOUTER CETTE ANNOTATION (nécessite l'installation de Lombok)
@Entity
public class Team {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
}