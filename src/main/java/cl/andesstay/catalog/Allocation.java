package cl.andesstay.catalog;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "AS_ALLOCATIONS")
public class Allocation {
    @Id @Column(length = 36) private String reservationId;
    @ManyToOne(optional = false) @JoinColumn(name = "unit_id") private Unit unit;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(nullable = false) private boolean active;
    protected Allocation() { }
    public Allocation(String reservationId, Unit unit, LocalDate startDate, LocalDate endDate) {
        this.reservationId = reservationId; this.unit = unit; this.startDate = startDate; this.endDate = endDate; this.active = true;
    }
    public String getReservationId() { return reservationId; }
    public String getUnitId() { return unit.getId(); }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
}
