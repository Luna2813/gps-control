CREATE TABLE IF NOT EXISTS suscripciones_push (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL,
    endpoint        TEXT NOT NULL,
    clave_p256dh    TEXT NOT NULL,
    clave_auth      TEXT NOT NULL,
    agente_usuario  VARCHAR(300),
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_push_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_push_endpoint UNIQUE (endpoint)
);

CREATE INDEX IF NOT EXISTS idx_push_usuario_activa
    ON suscripciones_push (usuario_id, activa);

ALTER TABLE notificaciones
    ADD COLUMN IF NOT EXISTS push_enviado_en TIMESTAMPTZ;
