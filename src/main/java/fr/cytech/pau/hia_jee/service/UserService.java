package fr.cytech.pau.hia_jee.service;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Enregistre un nouvel utilisateur.
     * Force le rôle PLAYER par défaut.
     */
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris !");
        }

        // C'est ici qu'on utilise l'Enum pour la sécurité
        user.setRole(Role.PLAYER);

        return userRepository.save(user);
    }

    /**
     * Authentification simple.
     */
    public User authenticate(String username, String password) {
        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();
            // Comparaison simple du mot de passe (en clair pour ce projet scolaire)
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Permet à un utilisateur de rejoindre une équipe.
     */
    @Transactional
    public void joinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Règle 1 : L'utilisateur ne doit pas déjà avoir une équipe
        if (user.getTeam() != null) {
            throw new RuntimeException("Vous appartenez déjà à une équipe ! Quittez-la d'abord.");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        // Règle 2 : (Optionnel) Vérifier si l'équipe est pleine (ex: max 5 joueurs)
        if (team.getMembers().size() >= 5) throw new RuntimeException("L'équipe est complète !");

        user.setTeam(team);

        userRepository.save(user);
    }

    /**
     * Quitter une équipe
     */
    public void leaveTeam(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setTeam(null);
        userRepository.save(user);
    }

    @Transactional
    public void kickMember(Long leaderId, Long memberId) {
        // 1. On récupère le chef (celui qui demande)
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new RuntimeException("Chef introuvable"));

        // 2. On récupère le membre à virer
        User memberToKick = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        // 3. On vérifie que le chef a bien une équipe
        Team team = leader.getTeam();
        if (team == null) {
            throw new RuntimeException("Vous n'avez pas d'équipe.");
        }

        // 4. SÉCURITÉ : On vérifie que le demandeur est bien le LEADER de l'équipe
        // (Attention aux NullPointerException, on vérifie que team.getLeader() existe)
        if (team.getLeader() == null || !team.getLeader().getId().equals(leader.getId())) {
            throw new RuntimeException("Seul le capitaine peut exclure un joueur.");
        }

        // 5. On vérifie que la victime est bien dans cette équipe
        if (memberToKick.getTeam() == null || !memberToKick.getTeam().getId().equals(team.getId())) {
            throw new RuntimeException("Ce joueur ne fait pas partie de votre équipe.");
        }

        // 6. On ne peut pas se virer soi-même (ça c'est "Quitter" ou "Dissoudre")
        if (leader.getId().equals(memberToKick.getId())) {
            throw new RuntimeException("Vous ne pouvez pas vous exclure vous-même.");
        }

        // ACTION : On retire l'équipe du membre
        memberToKick.setTeam(null);
        userRepository.save(memberToKick);
    }

    @Transactional
    public void joinTeamByInviteCode(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (user.getTeam() != null) {
            throw new RuntimeException("Vous avez déjà une équipe !");
        }

        // On cherche l'équipe via le code, pas via l'ID
        Team team = teamRepository.findByInviteCode(code)
                .orElseThrow(() -> new RuntimeException("Lien d'invitation invalide ou expiré."));

        // (Optionnel) Vérifier si l'équipe est pleine
        // if (team.getMembers().size() >= 5) throw ...

        user.setTeam(team);
        userRepository.save(user);
    }
}