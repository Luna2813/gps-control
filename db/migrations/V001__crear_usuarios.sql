CREATE TABLE IF NOT EXISTS usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(120) NOT NULL,
    usuario             VARCHAR(80) NOT NULL,
    password_hash       VARCHAR(100) NOT NULL,
    rol                 VARCHAR(20) NOT NULL DEFAULT 'OPERADOR',
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos   INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta     TIMESTAMPTZ,
    ultimo_acceso       TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuarios_usuario UNIQUE (usuario),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMIN', 'OPERADOR')),
    CONSTRAINT ck_usuarios_intentos CHECK (intentos_fallidos >= 0)
);

CREATE INDEX IF NOT EXISTS idx_usuarios_usuario_activo
    ON usuarios (usuario, activo);
