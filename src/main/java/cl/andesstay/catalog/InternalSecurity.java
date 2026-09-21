package cl.andesstay.catalog;

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

@Component
public class InternalSecurity extends OncePerRequestFilter {
    private final String token;
    public InternalSecurity(@Value("${internal.token}") String token) {
        if (token == null || token.isBlank()) throw new IllegalStateException("INTERNAL_TOKEN es obligatorio");
        this.token = token;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String supplied = request.getHeader("X-Internal-Token");
        boolean ok = supplied != null && MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
        if (!ok) { response.sendError(401, "Acceso interno requerido"); return; }
        chain.doFilter(request, response);
    }
}
