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

    // 1-N Relationship: One Team has many Members
    // "mappedBy" refers to the 'team' field in the User class
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> members = new ArrayList<>();

    // Constructors
    public Team() {}

    public Team(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}