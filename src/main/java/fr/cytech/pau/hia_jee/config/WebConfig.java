package fr.cytech.pau.hia_jee.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Classe de configuration globale pour Spring MVC.

@Configuration // Indique à Spring qu'il s'agit d'une classe de configuration (chargée au démarrage)
public class WebConfig implements WebMvcConfigurer {

    // Injection de l'intercepteur que nous avons créé précédemment.
    // Spring va chercher le composant @Component AdminInterceptor et l'injecter ici.
    @Autowired
    private AdminInterceptor adminInterceptor;

    //Méthode pour enregistrer les intercepteurs dans le registre de Spring MVC.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        // Enregistrement de l'AdminInterceptor
        // 🗣️ "CONFIGURATION : J'applique mon intercepteur de sécurité uniquement sur les routes /admin/**."
        registry.addInterceptor(adminInterceptor)
                // Définition des règles de filtrage :
                // L'intercepteur ne se déclenchera QUE si l'URL commence par "/admin/"
                .addPathPatterns("/admin/**"); 
        
        // Note sur les patterns :
        // "/admin/*"  -> Matcherait "/admin/users" MAIS PAS "/admin/users/edit"
        // "/admin/**" -> Match récursif : tout ce qui se trouve sous /admin/, peu importe la profondeur.
        
        // On pourrait aussi exclure certaines routes spécifiques si besoin :
        // .excludePathPatterns("/admin/login", "/admin/public-info");
    }
}