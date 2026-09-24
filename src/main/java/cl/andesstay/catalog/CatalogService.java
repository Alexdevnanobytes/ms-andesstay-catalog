package cl.andesstay.catalog;

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
    @Transactional(readOnly = true)
    public List<Unit> all() { return units.findAll(); }
    @Transactional(readOnly = true)
    public Unit one(String id) { return units.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente")); }
    @Transactional
    public Unit create(CatalogController.UnitRequest data) {
        return units.save(new Unit(data.code(), data.type(), data.description(), data.nightlyRate()));
    }
    @Transactional
    public Unit update(String id, CatalogController.UnitRequest data) {
        Unit unit = one(id);
        if (!unit.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad inactiva");
        unit.update(data.code(), data.type(), data.description(), data.nightlyRate());
        return unit;
    }
    @Transactional
    public void deactivate(String id) {
        Unit unit = units.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente"));
        if (allocations.existsByUnit_IdAndActiveTrue(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad tiene reservas activas");
        unit.deactivate();
    }
    public void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null || !from.isBefore(to))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La salida debe ser posterior a la entrada");
    }
    @Transactional(readOnly = true)
    public List<Unit> available(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        return units.findAll().stream().filter(u -> u.isActive() && allocations.overlapping(u.getId(), from, to).isEmpty()).toList();
    }
    @Transactional(readOnly = true)
    public boolean available(String id, LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        return one(id).isActive() && allocations.overlapping(id, from, to).isEmpty();
    }
    @Transactional
    public void allocate(CatalogController.AllocationRequest data) {
        validatePeriod(data.from(), data.to());
        Unit unit = units.lockById(data.unitId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad inexistente"));
        if (!unit.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad inactiva");
        var existing = allocations.findById(data.reservationId());
        if (existing.isPresent()) {
            Allocation a = existing.get();
            if (a.isActive() && a.getUnitId().equals(unit.getId()) && a.getStartDate().equals(data.from()) && a.getEndDate().equals(data.to())) return;
            if (!a.isActive() && a.getUnitId().equals(unit.getId()) && a.getStartDate().equals(data.from()) && a.getEndDate().equals(data.to())
                    && allocations.overlapping(unit.getId(), data.from(), data.to()).isEmpty()) { a.activate(); return; }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva ya asignada o fechas distintas");
        }
        if (!allocations.overlapping(unit.getId(), data.from(), data.to()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unidad ocupada en esas fechas");
        allocations.save(new Allocation(data.reservationId(), unit, data.from(), data.to()));
    }
    @Transactional
    public void release(String reservationId) {
        allocations.findById(reservationId).ifPresent(a -> {
            units.lockById(a.getUnitId());
            a.deactivate();
        });
    }
}
