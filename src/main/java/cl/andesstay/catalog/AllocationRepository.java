package cl.andesstay.catalog;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AllocationRepository extends JpaRepository<Allocation, String> {
    @Query("select a from Allocation a where a.unit.id = :unitId and a.active = true and a.startDate < :endDate and a.endDate > :startDate")
    List<Allocation> overlapping(@Param("unitId") String unitId, @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
    boolean existsByUnitIdAndActiveTrue(String unitId);
}
