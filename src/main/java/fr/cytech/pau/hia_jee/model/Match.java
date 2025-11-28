package fr.cytech.pau.hia_jee.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="matches")
public class Match {
    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
    @OneToOne
    @JoinColumn(name="newt_match_id")
    private Match nextMatch;
    @ManyToOne
    @JoinColumn(name="team_a_id")
    private Team teamA;
    @ManyToOne
    @JoinColumn(name="team_b_id")
    private Team teamB;
    @ManyToOne
    @JoinColumn(name="tournament_id")
    private Tournament tournament;
    private double cashPrize;
    private int scoreA;
    private int scoreB;
    @ManyToOne
    private Team winner;

    public double getCashPrize() {
        return cashPrize;
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

    public void setCashPrize(double cashPrize) {
        this.cashPrize = cashPrize;
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
