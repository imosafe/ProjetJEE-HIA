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
@JoinTable(
    name = "tournament_sponsor",
    joinColumns = @JoinColumn(name = "tournament_id"),
    inverseJoinColumns = @JoinColumn(name = "sponsor_id")
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

    public double getCashPrize() {
        return cashPrize;
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

    public void setCashPrize(double cashPrize) {
        this.cashPrize = cashPrize;
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
}

