-- Esquema de PostgreSQL para ProyectoRestaurantePrueba.
--
-- Este archivo documenta el esquema, pero NO lo ejecuta la aplicacion:
-- `spring.sql.init.mode` esta en `never` y `ddl-auto` es `update` en dev,
-- `validate` en produccion.
--
-- En desarrollo se ejecuta a mano con:
--   docker compose exec -T postgres psql -U restaurante -d restaurante < src/main/resources/schema.sql
--
-- Este archivo NO lleva datos. Sembrar usuarios o credenciales aqui publica
-- la credencial en el repositorio: el seed va en un archivo ignorado por git.

CREATE TABLE IF NOT EXISTS restaurante (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    nit       VARCHAR(32)  NOT NULL UNIQUE,
    direccion VARCHAR(200),
    telefono  VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS producto (
    id         BIGSERIAL   PRIMARY KEY,
    nombre     VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio     NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    disponible BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_producto_nombre ON producto (nombre);