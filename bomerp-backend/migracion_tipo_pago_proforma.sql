-- Ruta: bomerp-backend/migracion_tipo_pago_proforma.sql
-- CAMBIO SOLICITADO: persistir la forma de pago preferida en cada proforma.
-- Ejecutar una sola vez en instalaciones existentes con ddl-auto=validate.
-- Las proformas ya registradas conservan el comportamiento contado como valor inicial.

ALTER TABLE PROFORMAS
    ADD (TIPO_PAGO VARCHAR2(10 CHAR) DEFAULT 'CONTADO' NOT NULL);

ALTER TABLE PROFORMAS
    ADD CONSTRAINT CK_PROFORMAS_TIPO_PAGO CHECK (TIPO_PAGO IN ('CONTADO', 'CREDITO'));

COMMIT;

-- En nuevas proformas ProformaService guarda la opción elegida. Al convertir, esta opción se preselecciona;
-- el operador aún confirma la venta y los días de crédito antes de registrarla.
