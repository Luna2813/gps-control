CREATE TABLE IF NOT EXISTS clientes (
    id                      SERIAL PRIMARY KEY,
    nombre                  VARCHAR(160) NOT NULL,
    dpi                     VARCHAR(30) NOT NULL,
    nit                     VARCHAR(30),
    email                   VARCHAR(160),
    telefono                VARCHAR(40),
    cantidad_dispositivos   INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_clientes_dpi UNIQUE (dpi),
    CONSTRAINT ck_clientes_dispositivos
        CHECK (cantidad_dispositivos >= 0)
);

CREATE TABLE IF NOT EXISTS vehiculos_gps (
    id                      SERIAL PRIMARY KEY,
    cliente_id              INTEGER NOT NULL,
    vehiculo                VARCHAR(160) NOT NULL,
    placa                   VARCHAR(30) NOT NULL,
    fecha_instalacion       DATE NOT NULL,
    tipo_gps                VARCHAR(100),
    imei                    VARCHAR(40),
    telefonia               VARCHAR(80),
    numero_sim              VARCHAR(40),
    numero_telefono         VARCHAR(40),
    promocion               VARCHAR(30) NOT NULL DEFAULT 'Inactiva',
    fecha_fin_promocion     DATE,
    descripcion_promocion   VARCHAR(250),
    monto_normal            NUMERIC(12,2) NOT NULL DEFAULT 0,
    monto_promocion         NUMERIC(12,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_vehiculos_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_vehiculos_placa UNIQUE (placa),
    CONSTRAINT ck_vehiculos_monto_normal CHECK (monto_normal >= 0),
    CONSTRAINT ck_vehiculos_monto_promocion CHECK (monto_promocion >= 0)
);

CREATE INDEX IF NOT EXISTS idx_vehiculos_cliente
    ON vehiculos_gps (cliente_id);

CREATE INDEX IF NOT EXISTS idx_vehiculos_fin_promocion
    ON vehiculos_gps (fecha_fin_promocion)
    WHERE fecha_fin_promocion IS NOT NULL;

-- Tabla heredada. Se conserva para que InstalacionGPSResource continúe
-- funcionando durante la transición al modelo clientes + vehiculos_gps.
CREATE TABLE IF NOT EXISTS instalaciones_gps (
    id                      SERIAL PRIMARY KEY,
    nombre                  VARCHAR(160) NOT NULL,
    dpi                     VARCHAR(30),
    nit                     VARCHAR(30),
    email                   VARCHAR(160),
    cantidad_dispositivos   INTEGER NOT NULL DEFAULT 0,
    vehiculo                VARCHAR(160),
    placa                   VARCHAR(30) NOT NULL,
    fecha_instalacion       DATE NOT NULL,
    tipo_gps                VARCHAR(100),
    imei                    VARCHAR(40),
    telefonia               VARCHAR(80),
    numero_telefono         VARCHAR(40),
    numero_sim              VARCHAR(40),
    promocion               VARCHAR(30) NOT NULL DEFAULT 'Inactiva',
    fecha_fin_promocion     DATE,
    monto                   NUMERIC(12,2) NOT NULL DEFAULT 0,

    CONSTRAINT ck_instalaciones_dispositivos
        CHECK (cantidad_dispositivos >= 0),
    CONSTRAINT ck_instalaciones_monto CHECK (monto >= 0)
);
