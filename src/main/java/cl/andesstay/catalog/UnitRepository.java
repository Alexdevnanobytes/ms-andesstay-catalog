package cl.andesstay.catalog;

// Repositorio de las unidades (tabla AS_UNITS).
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitRepository extends JpaRepository<Unit, String> {
    // Lee una unidad bloqueando su fila hasta que termine la transacción (SELECT ... FOR UPDATE).
    // Evita que dos asignaciones simultáneas ocupen la misma unidad.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Unit u where u.id = :id")
    Optional<Unit> lockById(@Param("id") String id);
}
