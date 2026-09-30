-- Audit columns for all order tables are already present in V1.
-- Atomic yearly counter prevents duplicate display numbers under concurrent requests.
CREATE TABLE order_sequence (
    nam SMALLINT PRIMARY KEY,
    last_value BIGINT NOT NULL DEFAULT 0 CHECK (last_value >= 0)
);

ALTER TABLE don_hang ADD COLUMN ton_kho_da_tru BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE bien_the_san_pham ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE topping ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
