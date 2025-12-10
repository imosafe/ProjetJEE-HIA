package fr.cytech.pau.hia_jee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.Tournament;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import fr.cytech.pau.hia_jee.repository.TournamentRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;

/**
 * Service contenant la logique métier pour les Équipes.
 * Il gère les interactions complexes entre Team, User et Tournament.
 */
@Service
public class TeamService {

    @Autowired private TeamRepository teamRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TournamentRepository tournamentRepository;

    /**
     * Récupère toutes les équipes (pour la liste publique).
     */
    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * Crée une nouvelle équipe.
     * Transactionnelle car elle touche à la fois à l'équipe et potentiellement à l'état de l'utilisateur.
     */
    @Transactional
    public Team createTeam(Team team, User creator) {
        // 1. Validation métier : Unicité du nom
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("Le nom d'équipe est déjà pris !");
        }

        // 2. Réattachement de l'utilisateur au contexte de persistance.
        // L'objet 'creator' vient de la session HTTP, il est "détaché" d'Hibernate.
        // On le recharge depuis la BDD pour être sûr d'avoir la version la plus à jour et "gérée".
        User managedCreator = userRepository.findById(creator.getId())
                .orElseThrow(() -> new RuntimeException("Session invalide."));

        // 3. Logique métier : Le créateur devient le Chef (Leader)
        team.setLeader(managedCreator);
        
        // Note : Selon la configuration de vos cascades, il faudrait peut-être aussi faire :
        // team.getMembers().add(managedCreator);
        // managedCreator.setTeam(team);

        return teamRepository.save(team);
    }

    /**
     * Récupère une équipe avec ses membres chargés (Eager Fetching).
     * Indispensable pour éviter le LazyInitializationException dans la vue.
     */
    public Team findTeamWithMembers(Long id) {
        return teamRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));
    }

    /**
     * Récupération simple par ID.
     */
    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Équipe introuvable"));
    }

    /**
     * Dissout (supprime) une équipe.
     * Le transactional est important car on modifie plusieurs utilisateurs (les membres) avant de supprimer l'équipe.
     * Si ça plante au milieu, tout doit être annulé (Rollback).
     */
    @Transactional
    public void dissolveTeam(Long id) {
        Team team = teamRepository.findById(id).orElseThrow();

        // 1. Nettoyage de la relation Many-to-Many (Tournois)
        // On retire l'équipe des tournois pour éviter l'erreur de clé étrangère
        if (team.getTournaments() != null) {
            for (Tournament t : team.getTournaments()) {
                t.getTeams().remove(team);
            }
        }

        // 2. Nettoyage de la relation One-to-Many (Membres)
        // On libère les membres (setTeam(null)) pour ne pas les supprimer avec l'équipe
        // (Alternative sûre au CascadeType.REMOVE qui supprimerait les users)
        if (team.getMembers() != null) {
            for (User member : team.getMembers()) {
                member.setTeam(null);
                // Pas besoin de save(member) explicite grâce au Dirty Checking transactionnel
            }
        }

        // 3. Suppression propre de l'équipe
        teamRepository.delete(team);
    }

    /**
     * Trouve une équipe via son code d'invitation (UUID).
     */
    public Team findByInviteCode(String code) {
        return teamRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Code invalide"));
    }

    /**
     * Inscrit une équipe à un tournoi.
     */
    @Transactional
    public void registerTeamToTournament(Long teamId, Long tournamentId, User requester) {
        Team team = findById(teamId);
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournoi introuvable"));

        // --- VALIDATIONS MÉTIER ---

        // 1. Sécurité : Seul le chef peut inscrire l'équipe
        if (!team.getLeader().getId().equals(requester.getId())) {
            throw new RuntimeException("Seul le capitaine peut inscrire l'équipe.");
        }
        
        // 2. Cohérence : Le jeu de l'équipe doit correspondre au jeu du tournoi
        if (team.getGame() != tournament.getGame()) {
            throw new RuntimeException("Jeu incompatible.");
        }
        
        // 3. Doublon : L'équipe est-elle déjà inscrite ?
        if (tournament.getTeams().contains(team)) {
            throw new RuntimeException("Déjà inscrit.");
        }

        // On ajoute l'équipe à la liste des participants du tournoi.
        // il suffit de sauvegarder le tournoi pour que la ligne soit créée dans la table de jointure.
        tournament.getTeams().add(team);
        tournamentRepository.save(tournament);
    }
}