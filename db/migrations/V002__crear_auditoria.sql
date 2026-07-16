CREATE TABLE IF NOT EXISTS auditoria (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT,
    usuario         VARCHAR(80) NOT NULL,
    rol             VARCHAR(20) NOT NULL,
    metodo          VARCHAR(10) NOT NULL,
    ruta            VARCHAR(300) NOT NULL,
    estado_http     INTEGER NOT NULL,
    direccion_ip    VARCHAR(64),
    navegador       VARCHAR(300),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_creado_en
    ON auditoria (creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario
    ON auditoria (usuario_id, creado_en DESC);
