package cl.andesstay.catalog;

// Entidad JPA de la tabla AS_UNITS: una unidad alojable (habitación o cabaña).
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
// El código (por ejemplo "H-101") es único: la base de datos no permite dos unidades con el mismo código.
@Table(name = "AS_UNITS", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Unit {
    // Clave primaria: UUID como texto.
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 30) private String code;
    // Tipo de unidad guardado como texto: HABITACION o CABANA.
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UnitType type;
    @Column(nullable = false, length = 500) private String description;
    // Tarifa por noche: hasta 12 dígitos, 2 de ellos decimales (BigDecimal para dinero).
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal nightlyRate;
    // active = false significa "desactivada": no aparece como disponible ni acepta asignaciones.
    @Column(nullable = false) private boolean active = true;

    // Constructor vacío protegido que exige JPA.
    protected Unit() { }
    // Nueva unidad con UUID generado; nace activa.
    public Unit(String code, UnitType type, String description, BigDecimal nightlyRate) {
        this.id = UUID.randomUUID().toString(); update(code, type, description, nightlyRate);
    }
    // Actualiza los datos editables de la unidad.
    public void update(String code, UnitType type, String description, BigDecimal nightlyRate) {
        this.code = code; this.type = type; this.description = description; this.nightlyRate = nightlyRate;
    }
    // Getters: se usan para convertir la unidad a JSON.
    public String getId() { return id; }
    public String getCode() { return code; }
    public UnitType getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public boolean isActive() { return active; }
    // Baja lógica: la unidad queda inactiva, pero sigue en la base (se conserva el historial).
    public void deactivate() { this.active = false; }
}
