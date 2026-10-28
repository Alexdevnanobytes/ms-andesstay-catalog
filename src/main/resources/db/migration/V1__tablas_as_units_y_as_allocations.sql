-- Linea base: deja las tablas tal como las crea Hibernate hoy.
-- IF NOT EXISTS: en una base nueva las crea; en el RDS actual (ya existen) no cambia nada.
CREATE TABLE IF NOT EXISTS as_units (
    id            VARCHAR(36)   NOT NULL,
    code          VARCHAR(30)   NOT NULL,
    type          VARCHAR(20)   NOT NULL,
    description   VARCHAR(500)  NOT NULL,
    nightly_rate  NUMERIC(12,2) NOT NULL,
    active        BOOLEAN       NOT NULL,
    CONSTRAINT as_units_pkey PRIMARY KEY (id),
    CONSTRAINT as_units_code_key UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS as_allocations (
    reservation_id  VARCHAR(36) NOT NULL,
    unit_id         VARCHAR(36) NOT NULL,
    start_date      DATE        NOT NULL,
    end_date        DATE        NOT NULL,
    active          BOOLEAN     NOT NULL,
    CONSTRAINT as_allocations_pkey PRIMARY KEY (reservation_id),
    CONSTRAINT as_allocations_unit_fkey FOREIGN KEY (unit_id) REFERENCES as_units (id)
);