package fr.cytech.pau.hia_jee.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entité représentant un Tournoi.
 * C'est l'objet pivot qui relie les équipes, les sponsors et les matchs.
 */
@Data 
@Entity
@Table(name="tournaments")
public class Tournament {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name; // Nom du tournoi (ex: "Winter Cup 2024")

    // --- RELATION AVEC LES MATCHS ---
    // Un tournoi contient plusieurs matchs.
    // "mappedBy = tournament" : C'est l'entité Match qui possède la clé étrangère (tournament_id).
    // CascadeType.ALL : Si on supprime le tournoi, TOUS les matchs associés sont supprimés automatiquement de la BDD.
    @OneToMany(mappedBy="tournament", cascade=CascadeType.ALL)
    private List<Match> matches;

    // --- RELATION AVEC LES SPONSORS ---
    // Relation ManyToMany : Un tournoi a plusieurs sponsors, un sponsor finance plusieurs tournois.
    // @JoinTable : Indique que c'est CETTE classe (Tournament) qui est "propriétaire" de la relation.
    // Elle va créer une table intermédiaire "tournament_sponsor" contenant les ID des deux côtés.
    @ManyToMany 
    @JoinTable(
        name = "tournament_sponsor",
        joinColumns = @JoinColumn(name = "tournament_id"),      // Clé de cette classe
        inverseJoinColumns = @JoinColumn(name = "sponsor_id")   // Clé de l'autre classe
    )
    private List<Sponsor> Sponsors;

    // --- RELATION AVEC LES ÉQUIPES ---
    // C'est ici qu'on stocke les équipes inscrites.
    // Table de jointure : "tournament_teams".
    @ManyToMany
    @JoinTable(
        name="tournament_teams",
        joinColumns=@JoinColumn(name="tournament_id"),
        inverseJoinColumns = @JoinColumn(name="team_id")
    )
    private List<Team> teams;

    // --- ÉTAT DU TOURNOI ---
    // Statut (OUVERT, EN_COURS, TERMINE)
    // EnumType.STRING stocke le texte en BDD, plus sûr que ORDINAL (chiffres).
    @Enumerated(EnumType.STRING)
    private StatusTournament status;

    // --- JEU DU TOURNOI ---
    // Jeu concerné (LoL, Valorant, etc.)
    @Enumerated(EnumType.STRING)
    private Game game;

    // --- GETTERS ET SETTERS ---

    public Game getGame(){
        return game;
    }
    public void setGame(Game game){
        this.game=game;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public List<Sponsor> getSponsors() {
        return Sponsors;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Long getId() {
        return id;
    }

    public StatusTournament getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public void setSponsors(List<Sponsor> sponsors) {
        Sponsors = sponsors;
    }

    public void setStatus(StatusTournament status) {
        this.status = status;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}