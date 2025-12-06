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

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TournamentRepository tournamentRepository;

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
    //Inscription au tournoi
      @Transactional
    public void registerTeamToTournament(Long teamId, Long tournamentId, User requester) {
        
        // 1. Récupération des entités
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));
        
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        // 2. VÉRIFICATION : Est-ce que le demandeur est le CHEF ?
        if (!team.getLeader().getId().equals(requester.getId())) {
            throw new RuntimeException("Seul le capitaine peut inscrire l'équipe.");
        }

        // 3. VÉRIFICATION : Est-ce le bon JEU ?
        // On compare les chaines (ignorer la casse est plus prudent)
        if (!team.getGame().equalsIgnoreCase(tournament.getGame())) {
            throw new RuntimeException("Votre équipe joue à " + team.getGame() + 
                                     " mais le tournoi est sur " + tournament.getGame());
        }

        // 4. VÉRIFICATION : Déjà inscrit ?
        if (tournament.getTeams().contains(team)) {
            throw new RuntimeException("Votre équipe est déjà inscrite à ce tournoi.");
        }

        // 5. ACTION : Inscription
        // Dans une relation ManyToMany, on ajoute souvent des deux côtés, 
        // mais sauvegarder le "propriétaire" de la relation suffit.
        tournament.getTeams().add(team);
        
        tournamentRepository.save(tournament);
    }
}