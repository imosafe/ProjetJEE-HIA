package fr.cytech.pau.hia_jee.repository;

import fr.cytech.pau.hia_jee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Essentiel pour le Login : Retrouver un user par son pseudo
    // Renvoie un Optional pour éviter les NullPointerExceptions si le user n'existe pas
    Optional<User> findByUsername(String username);

    // Utile pour l'inscription : Vérifier si le pseudo est déjà pris
    boolean existsByUsername(String username);
}