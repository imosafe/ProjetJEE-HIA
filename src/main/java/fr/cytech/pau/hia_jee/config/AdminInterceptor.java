package fr.cytech.pau.hia_jee.config;

import fr.cytech.pau.hia_jee.model.Role;
import fr.cytech.pau.hia_jee.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Récupérer la session
        HttpSession session = request.getSession(false); // false = ne pas créer de nouvelle session
        // 2. Vérifier si la session existe et récupérer l'utilisateur
        if (session == null) {
            System.out.println("❌ [AdminInterceptor] Pas de session !");
            response.sendRedirect("/login?error=no_session");
            return false;
        }

        User user = (User) session.getAttribute("user");
        
        System.out.println("🔍 [AdminInterceptor] User from session: " + user);
        if (user != null) {
            System.out.println("   - Username: " + user.getUsername());
            System.out.println("   - Role: " + user.getRole());
            System.out.println("   - Role == ADMIN: " + (user.getRole() == Role.ADMIN));
        }

        // 3. Vérifier s'il y a un utilisateur et s'il est ADMIN
        if (user != null && user.getRole() != null && user.getRole() == Role.ADMIN) {
            System.out.println("✅ [AdminInterceptor] Admin autorisé !");
            return true;
        }

        // 4. Sinon, on bloque et on redirige
        System.out.println("❌ [AdminInterceptor] Accès refusé pour: " + (user != null ? user.getUsername() : "null"));
        response.sendRedirect("/login?error=access_denied");
        return false;
    }
}