package cl.andesstay.catalog;

// Repositorio de las asignaciones (tabla AS_ALLOCATIONS).
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AllocationRepository extends JpaRepository<Allocation, String> {
    // Asignaciones ACTIVAS de una unidad que se superponen con el período [startDate, endDate).
    // Dos períodos se superponen si uno empieza antes de que termine el otro y termina después de que empiece
    // (a.startDate < endDate y a.endDate > startDate). Si la lista no está vacía, la unidad está ocupada.
    // Como es "menor que" estricto, una salida y una entrada el mismo día NO chocan.
    @Query("select a from Allocation a where a.unit.id = :unitId and a.active = true and a.startDate < :endDate and a.endDate > :startDate")
    List<Allocation> overlapping(@Param("unitId") String unitId, @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
    // ¿La unidad tiene alguna asignación activa? Spring genera la consulta a partir del nombre del método;
    // "Unit_Id" le indica que navegue a unit.id. Se usa antes de desactivar una unidad.
    boolean existsByUnit_IdAndActiveTrue(String unitId);
}
