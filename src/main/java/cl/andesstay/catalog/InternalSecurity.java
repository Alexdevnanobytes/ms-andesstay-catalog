package cl.andesstay.catalog;

// Filtro de seguridad interna del microservicio.
// Este servicio NO valida el JWT del usuario: solo acepta llamadas que traigan el token interno
// (header X-Internal-Token), que conocen únicamente el BFF y los otros microservicios.
// Además, en Docker no publica su puerto, así que no es alcanzable desde internet.
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// OncePerRequestFilter: Spring ejecuta este filtro una vez por cada petición HTTP, antes del controlador.
@Component
public class InternalSecurity extends OncePerRequestFilter {
    private final String token;
    public InternalSecurity(@Value("${internal.token}") String token) {
        // Sin INTERNAL_TOKEN el servicio no arranca.
        if (token == null || token.isBlank()) throw new IllegalStateException("INTERNAL_TOKEN es obligatorio");
        this.token = token;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Token que envió quien hace la llamada.
        String supplied = request.getHeader("X-Internal-Token");
        // Compara con MessageDigest.isEqual, que tarda lo mismo sin importar cuántos caracteres coincidan.
        // Así un atacante no puede adivinar el token midiendo tiempos de respuesta (ataque de temporización).
        boolean ok = supplied != null && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
        // Si falta o no coincide: 401 y la petición no sigue.
        if (!ok) { response.sendError(401, "Acceso interno requerido"); return; }
        // Si coincide: la petición continúa hacia el controlador.
        chain.doFilter(request, response);
    }
}
