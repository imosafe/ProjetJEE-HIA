package fr.cytech.pau.hia_jee.service;

import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }

    public Team createTeam(Team team, User creator) {
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("Le nom d'équipe est déjà pris !");
        }

        // --- CORRECTION DE ROBUSTESSE ---
        // On recharge l'utilisateur depuis la BDD pour être sûr qu'il existe encore
        // (Gère le cas où la BDD a été reset mais pas la session)
        // Correction : Utilisation de 'creator.getId()' (paramètre de la méthode) au lieu de 'sessionCreator'
        User managedCreator = userRepository.findById(creator.getId())
                .orElseThrow(() -> new RuntimeException("Erreur critique : Votre session est invalide. Veuillez vous reconnecter."));

        // On définit le créateur comme Chef (Leader) en utilisant l'instance managée par Hibernate
        team.setLeader(managedCreator);

        return teamRepository.save(team);
    }

    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    // Récupère l'équipe ET les membres (évite LazyInitializationException)
    public Team findTeamWithMembers(Long id) {
        return teamRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));
    }

    // --- NOUVELLE MÉTHODE : DISSOUDRE ---
    @Transactional // Important : gère la suppression des liens en BDD
    public void dissolveTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        // 1. On détache tous les membres de l'équipe
        if (team.getMembers() != null) {
            for (User member : team.getMembers()) {
                member.setTeam(null);
                // Hibernate mettra à jour l'utilisateur grâce à @Transactional
            }
        }

        // 2. On supprime l'équipe
        teamRepository.delete(team);
    }

    public Team findByInviteCode(String code) {
        return teamRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Code invalide"));
    }
}