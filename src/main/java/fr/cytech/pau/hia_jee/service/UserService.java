package fr.cytech.pau.hia_jee.service;

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
     * Enregistre un nouvel utilisateur en base.
     * Vérifie d'abord si le pseudo est disponible.
     */
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        // Par défaut, on peut forcer le rôle PLAYER si ce n'est pas précisé
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("PLAYER");
        }
        return userRepository.save(user);
    }

    /**
     * Vérifie le couple pseudo/mot de passe.
     * Retourne le User si OK, sinon null.
     */
    public User authenticate(String username, String password) {
        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();
            // Comparaison simple (Pour un projet scolaire sans Spring Security avancé)
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Permet à un utilisateur de rejoindre une équipe.
     * Gère la relation 1-N.
     */
    public void joinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));

        // Règle métier : Un joueur ne peut pas être dans deux équipes
        if (user.getTeam() != null) {
            throw new RuntimeException("User is already in a team! Leave it first.");
        }

        user.setTeam(team);
        userRepository.save(user); // Sauvegarde la mise à jour de la clé étrangère
    }

    /**
     * Permet à un utilisateur de quitter son équipe.
     */
    public void leaveTeam(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setTeam(null);
        userRepository.save(user);
    }
}