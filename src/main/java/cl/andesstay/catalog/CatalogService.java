package cl.andesstay.catalog;

// Lógica de negocio del catálogo: unidades, disponibilidad y asignaciones.
// La regla principal: una unidad no puede asignarse a dos reservas con fechas superpuestas.
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private final UnitRepository units;
    private final AllocationRepository allocations;
    public CatalogService(UnitRepository units, AllocationRepository allocations) {
        this.units = units; this.allocations = allocations;
    }
    // Lista de todas las unidades.
    @Transactional(readOnly = true)
    public List<Unit> all() { return units.findAll(); }
    // Una unidad por id; 404 si no existe.
    @Transactional(readOnly = true)
    public Unit one(String id) { return units.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente")); }
    // Crea una unidad activa con un UUID nuevo.
    @Transactional
    public Unit create(CatalogController.UnitRequest data) {
        return units.save(new Unit(data.code(), data.type(), data.description(), data.nightlyRate()));
    }
    // Edita una unidad. No se permite editar unidades desactivadas (409).
    // No hace falta llamar a save: al terminar la transacción, JPA guarda los cambios de la entidad.
    @Transactional
    public Unit update(String id, CatalogController.UnitRequest data) {
        Unit unit = one(id);
        if (!unit.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad inactiva");
        unit.update(data.code(), data.type(), data.description(), data.nightlyRate());
        return unit;
    }
    // Desactiva una unidad (baja lógica). Bloquea su fila y, si tiene asignaciones activas, responde 409.
    @Transactional
    public void deactivate(String id) {
        Unit unit = units.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente"));
        if (allocations.existsByUnit_IdAndActiveTrue(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad tiene reservas activas");
        unit.deactivate();
    }
    // Valida un período: la salida debe ser posterior a la entrada (400 si no).
    public void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null || !from.isBefore(to))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La salida debe ser posterior a la entrada");
    }
    // Unidades disponibles en un período: activas y sin asignaciones que se superpongan.
    @Transactional(readOnly = true)
    public List<Unit> available(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        return units.findAll().stream().filter(u -> u.isActive() && allocations.overlapping(u.getId(), from, to).isEmpty()).toList();
    }
    // ¿Una unidad específica está disponible en un período?
    @Transactional(readOnly = true)
    public boolean available(String id, LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        return one(id).isActive() && allocations.overlapping(id, from, to).isEmpty();
    }
    // Asigna (ocupa) una unidad para una reserva. La llama reservations al confirmar.
    @Transactional
    public void allocate(CatalogController.AllocationRequest data) {
        validatePeriod(data.from(), data.to());
        // Bloquea la fila de la unidad (SELECT ... FOR UPDATE). Si dos confirmaciones llegan a la vez
        // para la misma unidad, la segunda espera y luego ve la asignación de la primera: así no hay doble reserva.
        Unit unit = units.lockById(data.unitId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente"));
        // Una unidad desactivada no acepta asignaciones.
        if (!unit.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad inactiva");
        // ¿Esta reserva ya tenía una asignación? Así la operación es idempotente (repetirla no duplica nada).
        var existing = allocations.findById(data.reservationId());
        if (existing.isPresent()) {
            Allocation a = existing.get();
            // Misma reserva, misma unidad y mismas fechas, y ya activa: no hay nada que hacer.
            if (a.isActive() && a.getUnitId().equals(unit.getId()) && a.getStartDate().equals(data.from()) && a.getEndDate().equals(data.to())) return;
            // Misma asignación pero liberada: se reactiva si la unidad sigue libre en esas fechas.
            if (!a.isActive() && a.getUnitId().equals(unit.getId()) && a.getStartDate().equals(data.from()) && a.getEndDate().equals(data.to())
                    && allocations.overlapping(unit.getId(), data.from(), data.to()).isEmpty()) { a.activate(); return; }
            // Cualquier otro caso (otra unidad u otras fechas para la misma reserva): 409.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva ya asignada o fechas distintas");
        }
        // Si otra reserva ya ocupa la unidad en fechas que se superponen: 409 "Unidad ocupada en esas fechas".
        if (!allocations.overlapping(unit.getId(), data.from(), data.to()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad ocupada en esas fechas");
        // Unidad libre: se guarda la nueva asignación activa.
        allocations.save(new Allocation(data.reservationId(), unit, data.from(), data.to()));
    }
    // Libera la unidad de una reserva (checkout o cancelación). Si no hay asignación, no hace nada.
    // Bloquea la unidad para no cruzarse con una asignación simultánea y marca la asignación como inactiva.
    @Transactional
    public void release(String reservationId) {
        allocations.findById(reservationId).ifPresent(a -> {
            units.lockById(a.getUnitId());
            a.deactivate();
        });
    }
}
