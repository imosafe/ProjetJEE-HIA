package fr.cytech.pau.hia_jee.service;

import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    @Autowired private TeamRepository teamRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TournamentRepository tournamentRepository;

    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }
    @Transactional
    public Team createTeam(Team team, User creator) {
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("Le nom d'équipe est déjà pris !");
        }
        User managedCreator = userRepository.findById(creator.getId())
                .orElseThrow(() -> new RuntimeException("Session invalide."));
        team.setLeader(managedCreator);
        return teamRepository.save(team);
    }

    public Team findTeamWithMembers(Long id) {
        return teamRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));
    }

    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Équipe introuvable"));
    }

    @Transactional
    public void dissolveTeam(Long teamId) {
        Team team = findById(teamId);
        if (team.getMembers() != null) {
            for (User member : team.getMembers()) {
                member.setTeam(null);
            }
        }
        teamRepository.delete(team);
    }

    public Team findByInviteCode(String code) {
        return teamRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Code invalide"));
    }

    @Transactional
    public void registerTeamToTournament(Long teamId, Long tournamentId, User requester) {
        Team team = findById(teamId);
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        if (!team.getLeader().getId().equals(requester.getId())) {
            throw new RuntimeException("Seul le capitaine peut inscrire l'équipe.");
        }
        if (team.getGame() != tournament.getGame()) {
            throw new RuntimeException("Jeu incompatible.");
        }
        if (tournament.getTeams().contains(team)) {
            throw new RuntimeException("Déjà inscrit.");
        }

        tournament.getTeams().add(team);
        tournamentRepository.save(tournament);
    }
}