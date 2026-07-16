ALTER TABLE vehiculos_gps
    ADD COLUMN IF NOT EXISTS tipo_plan VARCHAR(20) NOT NULL DEFAULT 'MENSUAL',
    ADD COLUMN IF NOT EXISTS fecha_fin_plan_anual DATE,
    ADD COLUMN IF NOT EXISTS estado_plan_anual VARCHAR(20) NOT NULL DEFAULT 'NO_APLICA';

UPDATE vehiculos_gps
SET estado_plan_anual = CASE
    WHEN tipo_plan = 'ANUAL' AND fecha_fin_plan_anual < CURRENT_DATE THEN 'VENCIDO'
    WHEN tipo_plan = 'ANUAL' THEN 'ACTIVO'
    ELSE 'NO_APLICA'
END;

CREATE INDEX IF NOT EXISTS idx_vehiculos_fin_plan_anual
    ON vehiculos_gps (fecha_fin_plan_anual)
    WHERE tipo_plan = 'ANUAL' AND fecha_fin_plan_anual IS NOT NULL;

CREATE TABLE IF NOT EXISTS notificaciones (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(40) NOT NULL,
    titulo          VARCHAR(160) NOT NULL,
    mensaje         VARCHAR(500) NOT NULL,
    vehiculo_id     INTEGER,
    cliente_id      INTEGER,
    fecha_evento    DATE NOT NULL,
    leida           BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notificacion_vehiculo
        FOREIGN KEY (vehiculo_id) REFERENCES vehiculos_gps(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_notificacion_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON DELETE SET NULL,
    CONSTRAINT uq_notificacion_evento
        UNIQUE (tipo, vehiculo_id, fecha_evento)
);

CREATE INDEX IF NOT EXISTS idx_notificaciones_pendientes
    ON notificaciones (leida, creado_en DESC);
