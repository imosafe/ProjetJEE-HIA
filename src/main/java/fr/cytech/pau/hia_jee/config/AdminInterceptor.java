package fr.cytech.pau.hia_jee.config;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//Intercepteur de sécurité pour protéger les routes administrateur.

@Component
public class AdminInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. Récupérer la session HTTP existante
        // 'false' est crucial ici : on ne veut pas créer une nouvelle session vide
        // si l'utilisateur n'en a pas déjà une. On veut juste récupérer l'existante.
        HttpSession session = request.getSession(false); 

        // 2. Vérifier si la session existe (l'utilisateur est-il passé par le login ?)
        if (session == null) {
            System.out.println("❌ [AdminInterceptor] Pas de session !");
            // Redirection vers la page de login avec un code d'erreur
            response.sendRedirect("/login?error=no_session");
            return false; // On bloque la requête ici
        }

        // Récupération de l'objet User stocké en session (nécessite un cast explicite)
        User user = (User) session.getAttribute("user");
        
        // Logs de débogage pour tracer ce qui se passe dans la console serveur
        System.out.println("🔍 [AdminInterceptor] User from session: " + user);
        if (user != null) {
            System.out.println("   - Username: " + user.getUsername());
            System.out.println("   - Role: " + user.getRole());
            // Vérification booléenne affichée dans la console
            System.out.println("   - Role == ADMIN: " + (user.getRole() == Role.ADMIN));
        }

        // 3. Vérification stricte des droits
        // - L'utilisateur ne doit pas être null (session existante mais attribut manquant ?)
        // - Le rôle ne doit pas être null
        // - Le rôle DOIT être ADMIN
        if (user != null && user.getRole() != null && user.getRole() == Role.ADMIN) {
            System.out.println("✅ [AdminInterceptor] Admin autorisé !");
            return true; // Tout est bon, on laisse passer la requête vers le Controller
        }

        // 4. Si on arrive ici, l'utilisateur est connecté mais n'est PAS Admin (ou user null)
        System.out.println("❌ [AdminInterceptor] Accès refusé pour: " + (user != null ? user.getUsername() : "null"));
        
        // Redirection vers login (ou on pourrait rediriger vers une page 403 Forbidden)
        response.sendRedirect("/login?error=access_denied");
        
        return false; // On bloque la requête, le contrôleur ne sera jamais appelé
    }
}