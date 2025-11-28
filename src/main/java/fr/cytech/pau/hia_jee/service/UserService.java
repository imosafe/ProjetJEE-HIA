package fr.cytech.pau.hia_jee.service;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.Team;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.TeamRepository;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void joinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        // Règle métier : Un joueur ne peut pas être dans deux équipes
        if (user.getTeam() != null) {
            throw new RuntimeException("Vous êtes déjà dans une équipe ! Quittez-la d'abord.");
        }

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
}