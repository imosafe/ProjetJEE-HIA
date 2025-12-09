package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entité représentant un Match unique dans un tournoi.
 * Un match oppose deux équipes (Team A vs Team B) et possède un score.
 * * Cette classe contient aussi la logique de structure d'arbre (Bracket)
 * grâce à la relation "nextMatch".
 */
@Data 
@Entity
@Table(name="matches")
public class Match {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /**
     * Ce champ pointe vers le match SUIVANT dans l'arbre.
     */
    @ManyToOne
    @JoinColumn(name="new_match_id")
    private Match nextMatch;

    // Équipe 1 (peut être null au début du tournoi si l'arbre n'est pas rempli)
    @ManyToOne
    @JoinColumn(name="team_a_id")
    private Team teamA;

    // Équipe 2
    @ManyToOne
    @JoinColumn(name="team_b_id")
    private Team teamB;

    // Le tournoi auquel ce match appartient
    @ManyToOne
    @JoinColumn(name="tournament_id")
    private Tournament tournament;

    // Numéro du tour 
    // Utile pour trier et afficher l'arbre colonnes par colonnes.
    private int round;

    // Scores
    private int scoreA;
    private int scoreB;

    // Le vainqueur officiel (calculé après saisie du score)
    @ManyToOne
    private Team winner;

    // --- GETTERS ET SETTERS ---

    public int getRound() { 
        return round; 
    }
    public void setRound(int round) { 
        this.round = round; 
    }

    public Long getId() {
        return id;
    }

    public int getScoreA() {
        return scoreA;
    }

    public Match getNextMatch() {
        return nextMatch;
    }

    public int getScoreB() {
        return scoreB;
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public Team getWinner() {
        return winner;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setScoreA(int scoreA) {
        this.scoreA = scoreA;
    }

    public void setNextMatch(Match nextMatch) {
        this.nextMatch = nextMatch;
    }

    public void setScoreB(int scoreB) {
        this.scoreB = scoreB;
    }

    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }

    public void setTeamB(Team teamB) {
        this.teamB = teamB;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void setWinner(Team winner) {
        this.winner = winner;
    }
}