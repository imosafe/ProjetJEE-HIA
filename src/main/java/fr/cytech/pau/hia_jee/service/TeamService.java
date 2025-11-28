package fr.cytech.pau.hia_jee.service;


import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }

    public Team createTeam(Team team) {
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("Team name already taken!");
        }
        return teamRepository.save(team);
    }

    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }
}