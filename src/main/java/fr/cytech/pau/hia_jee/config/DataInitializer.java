package fr.cytech.pau.hia_jee.config;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Optional;

/**
 * Classe de configuration exécutée au démarrage de l'application.
 * Elle sert à "pré-remplir" la base de données avec des données essentielles,
 * ici : la création automatique du compte Super Admin.
 */
@Configuration // Indique à Spring qu'il faut scanner et charger cette classe au lancement
public class DataInitializer {

    /**
     * Définit un Bean CommandLineRunner.
     * Spring Boot exécute automatiquement la méthode run() de tous les beans CommandLineRunner
     * une fois que le contexte de l'application est totalement chargé.
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
             // 1. On vérifie si l'admin existe DÉJÀ en base de données
            // (C'est crucial pour ne pas créer un doublon ou écraser un compte existant à chaque redémarrage)
            // Note : findByUsername renvoie maintenant un Optional<User> ou User selon ton Repo, ici on assume Optional
           Optional<User> adminUserOptional = userRepository.findByUsername("admin");
            // Si findByUsername renvoie directement l'objet (et null s'il n'existe pas), adapte la condition :
            // if (existingAdmin == null) { ... }
            
            // Si on utilise Optional (recommandé) :
            // Optional<User> adminOptional = Optional.ofNullable(existingAdmin); 
            // if (adminOptional.isEmpty()) { ... }

            // Si ton repo renvoie directement User et que tu gères le null :
            if (adminUserOptional.isEmpty()) { 
                System.out.println("⚠️ Admin introuvable... Création du compte Admin par défaut.");

                // 2. Création de l'objet User
                User admin = new User();
                admin.setUsername("admin");
                
                // ATTENTION : Pour un vrai projet, le mot de passe devrait être hashé ici !
                // Ex: admin.setPassword(passwordEncoder.encode("admin"));
                admin.setPassword("admin"); 

                // 3. Assignation du rôle ADMIN (C'est le plus important ici)
                admin.setRole(Role.ADMIN);

                // 4. Sauvegarde en base de données
                userRepository.save(admin);

                System.out.println("✅ Compte Admin créé avec succès ! (User: admin / Pass: admin)");
            } else {
                // 5. Cas où l'admin existe déjà (ex: 2ème lancement du serveur)
                System.out.println("ℹ️ Le compte Admin existe déjà. Aucune action requise.");
            }
        };
    }
}