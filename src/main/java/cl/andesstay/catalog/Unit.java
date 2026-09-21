package cl.andesstay.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "AS_UNITS", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Unit {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 30) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UnitType type;
    @Column(nullable = false, length = 500) private String description;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal nightlyRate;
    @Column(nullable = false) private boolean active = true;

    protected Unit() { }
    public Unit(String code, UnitType type, String description, BigDecimal nightlyRate) {
        this.id = UUID.randomUUID().toString(); update(code, type, description, nightlyRate);
    }
    public void update(String code, UnitType type, String description, BigDecimal nightlyRate) {
        this.code = code; this.type = type; this.description = description; this.nightlyRate = nightlyRate;
    }
    public String getId() { return id; }
    public String getCode() { return code; }
    public UnitType getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
}
