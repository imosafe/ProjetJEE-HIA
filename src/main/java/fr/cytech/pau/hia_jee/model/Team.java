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

/**
 * Entité représentant une Équipe (Team).
 * Une équipe est composée de plusieurs joueurs (User), possède un Capitaine (Leader),
 * se spécialise dans un Jeu (Game) et participe à des Tournois.
 */
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Le nom doit être unique en base de données pour éviter les doublons
    @Column(nullable = false, unique = true)
    private String name;

    private String logoUrl;

    /**
     * Code unique permettant de rejoindre l'équipe via un lien direct.
     * Généré automatiquement à la création via UUID.
     */
    @Column(unique = true)
    private String inviteCode;

    // --- SPÉCIALISATION DE L'ÉQUIPE ---
    // Ce champ est crucial pour le filtrage : 
    // Une équipe de "LoL" ne doit pas pouvoir s'inscrire à un tournoi "FIFA".
    @Enumerated(EnumType.STRING)
    private Game game; // "League of Legends", "Valorant", etc.

    // --- RELATION 1 ---
    // Une équipe a PLUSIEURS membres.
    // "mappedBy = team" signifie que la relation est dirigée par l'attribut "team" dans la classe User.
    // CascadeType.ALL : Si on supprime l'équipe, on supprime/modifie les users associés (attention à la config ici).
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<User> members = new ArrayList<>();

    // --- RELATION 2 ---
    // Une équipe participe à PLUSIEURS tournois.
    // "mappedBy = teams" fait référence à la liste "private List<Team> teams" dans la classe Tournament.
    // C'est le Tournament qui est le "propriétaire" de la relation (qui gère la table de jointure).
    @ManyToMany(mappedBy = "teams") 
    private List<Tournament> tournaments = new ArrayList<>();

    // --- RELATION 3 ---
    // Une équipe a UN SEUL leader (Chef d'équipe).
    // Ce leader a des droits spéciaux (dissoudre l'équipe, virer des membres, inscrire aux tournois).
    @OneToOne
    private User leader;

    // --- CONSTRUCTEUR ---
    
    public Team() {
        // Génération automatique d'un code unique (UUID) dès qu'on fait "new Team()".
        // Ex: "550e8400-e29b-41d4-a716-446655440000"
        this.inviteCode = UUID.randomUUID().toString();
    }

    // --- GETTERS & SETTERS ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

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