package fr.cytech.pau.hia_jee.config;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.User;
import fr.cytech.pau.hia_jee.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            Optional<User> adminUser = userRepository.findByUsername("admin");

            if (adminUser.isEmpty()) {
                System.out.println("⚠️ Admin introuvable... Création du compte Admin par défaut.");

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin"); // Mot de passe par défaut

                admin.setRole(Role.ADMIN);


                userRepository.save(admin);

                System.out.println("✅ Compte Admin créé avec succès ! (User: admin / Pass: admin)");
            } else {
                System.out.println("ℹ️ Le compte Admin existe déjà.");
            }
        };
    }
}