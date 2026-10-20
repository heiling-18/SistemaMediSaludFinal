-- Migracion: agrega la columna motivo_cancelacion a la tabla citas
-- Conserva los datos existentes y permite NULL para citas canceladas previamente (RN6).

USE medisalud;

ALTER TABLE citas
    ADD COLUMN motivo_cancelacion VARCHAR(200) NULL;
