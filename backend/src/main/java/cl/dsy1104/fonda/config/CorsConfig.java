package cl.dsy1104.fonda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autoriza al frontend (otro puerto = otro origen) a llamar a la API desde el
 * navegador. El origen se lee de application.properties, no se escribe aqui.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String origenFrontend;

    public CorsConfig(@Value("${fonda.cors.origen}") String origenFrontend) {
        this.origenFrontend = origenFrontend;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenFrontend)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Sin esto, fetch desde otro origen no puede leer la cabecera Location.
                .exposedHeaders("Location");
    }
}
