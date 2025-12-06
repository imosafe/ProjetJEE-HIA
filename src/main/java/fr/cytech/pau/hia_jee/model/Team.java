package fr.cytech.pau.hia_jee.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String logoUrl;

    @Column(unique = true)
    private String inviteCode;

    // --- C'EST CE CHAMP QUI MANQUE ---
    @Enumerated(EnumType.STRING)
    private Game game; // "League of Legends", "Valorant", etc.

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> members = new ArrayList<>();

    @ManyToMany(mappedBy = "teams") // "teams" doit correspondre au nom de la liste dans Tournament.java
    private List<Tournament> tournaments = new ArrayList<>();

    @OneToOne
    private User leader;

    // Constructeurs
    public Team() {
        this.inviteCode = UUID.randomUUID().toString();
    }

    // Getters & Setters (Indispensables pour Thymeleaf !)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    // --- ET SURTOUT CE GETTER ---
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public User getLeader() { return leader; }
    public void setLeader(User leader) { this.leader = leader; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public List<Tournament> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<Tournament> tournaments) {
        this.tournaments = tournaments;
    }
}