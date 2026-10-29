package cl.andesstay.catalog;

// Entidad JPA de la tabla AS_ALLOCATIONS: una "asignación" dice que una unidad está ocupada
// por una reserva entre dos fechas. Se crea cuando reservations confirma una reserva.
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "AS_ALLOCATIONS")
public class Allocation {
    // La clave primaria es el id de la reserva: cada reserva tiene como máximo una asignación.
    @Id @Column(length = 36) private String reservationId;
    // Relación muchos-a-uno con la unidad: muchas asignaciones pueden apuntar a la misma unidad
    // (en fechas distintas). Se guarda como la columna unit_id.
    @ManyToOne(optional = false) @JoinColumn(name = "unit_id") private Unit unit;
    // Período ocupado: desde startDate (entrada) hasta endDate (salida).
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    // active = false cuando la unidad se libera (checkout o cancelación). La fila no se borra.
    @Column(nullable = false) private boolean active;
    // Constructor vacío protegido que exige JPA.
    protected Allocation() { }
    // Nueva asignación, activa desde el inicio.
    public Allocation(String reservationId, Unit unit, LocalDate startDate, LocalDate endDate) {
        this.reservationId = reservationId; this.unit = unit; this.startDate = startDate; this.endDate = endDate; this.active = true;
    }
    public String getReservationId() { return reservationId; }
    public String getUnitId() { return unit.getId(); }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    // Reactiva una asignación liberada (se usa si la misma reserva vuelve a asignarse con los mismos datos).
    public void activate() { this.active = true; }
    // Libera la unidad para ese período.
    public void deactivate() { this.active = false; }
}
