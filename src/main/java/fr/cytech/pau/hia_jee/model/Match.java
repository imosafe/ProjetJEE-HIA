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
    @OneToOne
    @JoinColumn(name="tournament_id")
    private Tournament tournament;
    private double cashPrize;
    private int scoreA;
    private int scoreB;
    @ManyToOne
    private Team winner;
}
