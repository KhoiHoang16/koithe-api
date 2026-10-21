ALTER TABLE don_hang
    ADD COLUMN guest_checkout_token VARCHAR(64);

CREATE INDEX idx_don_hang_guest_checkout_token
    ON don_hang (guest_checkout_token)
    WHERE guest_checkout_token IS NOT NULL;
