package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String logoUrl;

    // --- C'EST CE CHAMP QUI MANQUE ---
    @Column(nullable = false)
    private String game; // "League of Legends", "Valorant", etc.

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> members = new ArrayList<>();

    // Constructeurs
    public Team() {}

    // Getters & Setters (Indispensables pour Thymeleaf !)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    // --- ET SURTOUT CE GETTER ---
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}