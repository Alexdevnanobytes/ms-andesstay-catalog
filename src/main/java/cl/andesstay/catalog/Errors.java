package cl.andesstay.catalog;

// Manejo global de errores del catálogo.
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice: aplica a todos los controladores de este servicio.
@RestControllerAdvice
class Errors {
    // Si la base de datos rechaza una escritura por una restricción (el código de unidad es único en AS_UNITS),
    // en vez de un error 500 se responde 409 Conflict con el mensaje "Código de unidad repetido".
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Código de unidad repetido"));
    }
}
