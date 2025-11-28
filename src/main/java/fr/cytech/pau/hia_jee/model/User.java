package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users") // Avoids SQL reserved keyword "USER"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Replaces "pseudo"

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // IMPORTANT : Stocke "ADMIN" en texte dans la BDD, pas le chiffre 0
    private Role role;

    // N-1 Relationship: Many Users belong to One Team
    @ManyToOne
    @JoinColumn(name = "team_id") // This creates the foreign key column
    private Team team;

    // Constructors
    public User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}