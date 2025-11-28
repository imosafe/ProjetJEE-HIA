package fr.cytech.pau.hia_jee.config; // Crée ce package ou adapte-le

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
        HttpSession session = request.getSession();

        // 2. Récupérer l'utilisateur connecté
        User user = (User) session.getAttribute("user");

        // 3. Vérifier s'il est ADMIN
        if (user != null && user.getRole() == Role.ADMIN) {
            return true; // C'est bon, on laisse passer la requête vers le Controller
        }

        // 4. Sinon, on bloque et on redirige
        // On redirige vers l'accueil ou une page d'erreur "Accès Interdit"
        response.sendRedirect("/login?error=access_denied");
        return false; // On coupe la requête ici
    }
}