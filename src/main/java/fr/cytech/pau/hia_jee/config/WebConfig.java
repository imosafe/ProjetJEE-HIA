package fr.cytech.pau.hia_jee.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // On applique l'intercepteur uniquement sur les URLs commençant par /admin/...
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
        // Le "**" signifie "tout ce qu'il y a après" (sponsors, tournaments, etc.)
    }
}