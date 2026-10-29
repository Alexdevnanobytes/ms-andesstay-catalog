package cl.andesstay.catalog;

// Controlador REST del catálogo. Solo lo llaman el BFF (/api/...) y reservations (/internal/...),
// siempre con el header X-Internal-Token, que verifica el filtro InternalSecurity.
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) { this.service = service; }
    // Cuerpo para crear o editar una unidad. type debe ser HABITACION o CABANA;
    // la tarifa por noche debe ser al menos 0.01. Si algo falla, 400 Bad Request.
    public record UnitRequest(@NotBlank String code, @NotNull UnitType type, @NotBlank String description,
                              @NotNull @DecimalMin("0.01") BigDecimal nightlyRate) { }
    // Cuerpo para asignar una unidad a una reserva (lo envía reservations al confirmar).
    public record AllocationRequest(@NotBlank String reservationId, @NotBlank String unitId,
                                    @NotNull LocalDate from, @NotNull LocalDate to) { }

    // GET /api/catalog/units: todas las unidades (activas e inactivas).
    @GetMapping("/api/catalog/units") public List<Unit> all() { return service.all(); }
    // GET /api/catalog/units/{id}: una unidad (404 si no existe).
    @GetMapping("/api/catalog/units/{id}") public Unit one(@PathVariable String id) { return service.one(id); }
    // POST /api/catalog/units: crea una unidad (201). Si el código ya existe, Errors responde 409.
    @PostMapping("/api/catalog/units") @ResponseStatus(HttpStatus.CREATED)
    public Unit create(@Valid @RequestBody UnitRequest data) { return service.create(data); }
    // PUT /api/catalog/units/{id}: edita una unidad activa (409 si está inactiva).
    @PutMapping("/api/catalog/units/{id}") public Unit update(@PathVariable String id, @Valid @RequestBody UnitRequest data) { return service.update(id, data); }
    // DELETE /api/catalog/units/{id}: NO borra la fila, la desactiva (204).
    // 409 si la unidad tiene reservas activas.
    @DeleteMapping("/api/catalog/units/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) { service.deactivate(id); }
    // GET /api/catalog/available?from=&to=: unidades activas y libres en ese período.
    @GetMapping("/api/catalog/available")
    public List<Unit> available(@RequestParam LocalDate from, @RequestParam LocalDate to) { return service.available(from, to); }
    // GET /api/catalog/units/{id}/availability?from=&to=: responde {"available": true/false}.
    @GetMapping("/api/catalog/units/{id}/availability")
    public Map<String, Boolean> available(@PathVariable String id, @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return Map.of("available", service.available(id, from, to));
    }
    // Rutas internas, usadas solo por reservations:
    // POST /internal/allocations: ocupa una unidad para una reserva (204; 409 si está ocupada).
    @PostMapping("/internal/allocations") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void allocate(@Valid @RequestBody AllocationRequest data) { service.allocate(data); }
    // DELETE /internal/allocations/{reservationId}: libera la unidad de esa reserva (204).
    @DeleteMapping("/internal/allocations/{reservationId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable String reservationId) { service.release(reservationId); }
}
