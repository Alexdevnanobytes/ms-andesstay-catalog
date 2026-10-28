-- Restricciones que el servicio ya exige (UnitType, @DecimalMin en la tarifa, validatePeriod).
-- Cada bloque agrega la restriccion solo si no existe con ese nombre, por si Hibernate
-- ya creo alguna en el RDS.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'as_units_type_check'
          AND conrelid = 'as_units'::regclass
    ) THEN
        ALTER TABLE as_units
            ADD CONSTRAINT as_units_type_check
            CHECK (type IN ('HABITACION', 'CABANA'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'as_units_nightly_rate_check'
          AND conrelid = 'as_units'::regclass
    ) THEN
        ALTER TABLE as_units
            ADD CONSTRAINT as_units_nightly_rate_check
            CHECK (nightly_rate > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'as_allocations_period_check'
          AND conrelid = 'as_allocations'::regclass
    ) THEN
        ALTER TABLE as_allocations
            ADD CONSTRAINT as_allocations_period_check
            CHECK (end_date > start_date);
    END IF;
END $$;

-- AllocationRepository.overlapping y existsByUnit_IdAndActiveTrue: asignaciones activas por unidad y fechas.
CREATE INDEX IF NOT EXISTS idx_as_allocations_unit_dates_active
    ON as_allocations (unit_id, start_date, end_date)
    WHERE active;