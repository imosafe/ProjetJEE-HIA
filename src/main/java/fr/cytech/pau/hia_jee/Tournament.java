package fr.cytech.pau.hia_jee;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tournaments")
public class Tournament {
    
    public enum StatusTournament{OUVERT,EN_COURS,TERMINE};
    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
    @OneToMany(mappedBy="tournament",cascade=CascadeType.ALL)
    private List<Match> matches;
    @ManyToMany
    @JoinTable(name="tournament_sponsors",
        joinColumns = @JoinColumn(name="tournament_id"),
        inverseJoinColumns=@JoinColumn(name="sponsor_id")
    )
    private List<Sponsor> Sponsors;
    @ManyToMany
    @JoinTable(
        name="tournament_teams",
        joinColumns=@JoinColumn(name="tournament_id"),
        inverseJoinColumns = @JoinColumn(name="team_id")
    )
    private List<Team> teams;
     @Enumerated(EnumType.STRING)
    private StatusTournament status;
    private double cashPrize;
   
}
