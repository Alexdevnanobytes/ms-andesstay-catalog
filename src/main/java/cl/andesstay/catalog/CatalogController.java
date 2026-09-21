package cl.andesstay.catalog;

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
    public record UnitRequest(@NotBlank String code, @NotNull UnitType type, @NotBlank String description,
                              @NotNull @DecimalMin("0.01") BigDecimal nightlyRate) { }
    public record AllocationRequest(@NotBlank String reservationId, @NotBlank String unitId,
                                    @NotNull LocalDate from, @NotNull LocalDate to) { }

    @GetMapping("/api/catalog/units") public List<Unit> all() { return service.all(); }
    @GetMapping("/api/catalog/units/{id}") public Unit one(@PathVariable String id) { return service.one(id); }
    @PostMapping("/api/catalog/units") @ResponseStatus(HttpStatus.CREATED)
    public Unit create(@Valid @RequestBody UnitRequest data) { return service.create(data); }
    @PutMapping("/api/catalog/units/{id}") public Unit update(@PathVariable String id, @Valid @RequestBody UnitRequest data) { return service.update(id, data); }
    @DeleteMapping("/api/catalog/units/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) { service.deactivate(id); }
    @GetMapping("/api/catalog/available")
    public List<Unit> available(@RequestParam LocalDate from, @RequestParam LocalDate to) { return service.available(from, to); }
    @GetMapping("/api/catalog/units/{id}/availability")
    public Map<String, Boolean> available(@PathVariable String id, @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return Map.of("available", service.available(id, from, to));
    }
    @PostMapping("/internal/allocations") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void allocate(@Valid @RequestBody AllocationRequest data) { service.allocate(data); }
    @DeleteMapping("/internal/allocations/{reservationId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable String reservationId) { service.release(reservationId); }
}
