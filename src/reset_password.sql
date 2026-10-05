-- Reset mật khẩu cho toàn bộ tài khoản nội bộ trong bảng nguoi_dung.
-- Mật khẩu sau khi chạy: 123456
-- Cần có extension pgcrypto để tạo BCrypt hash (strength 10), tương thích
-- BCryptPasswordEncoder trong ứng dụng.
--
-- Chạy bằng psql, ví dụ:
--   psql "$env:DB_URL" -f scripts/reset-all-user-password.sql
--
-- Xem số tài khoản sẽ bị ảnh hưởng trước khi xác nhận COMMIT.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

SELECT count(*) AS so_tai_khoan_se_reset
FROM nguoi_dung
WHERE deleted_at IS NULL;

UPDATE nguoi_dung
SET mat_khau_ma_hoa = crypt('123456', gen_salt('bf', 10)),
    updated_at = CURRENT_TIMESTAMP
WHERE deleted_at IS NULL;

SELECT count(*) AS so_tai_khoan_co_mat_khau_moi
FROM nguoi_dung
WHERE deleted_at IS NULL
  AND mat_khau_ma_hoa = crypt('123456', mat_khau_ma_hoa);

COMMIT;
