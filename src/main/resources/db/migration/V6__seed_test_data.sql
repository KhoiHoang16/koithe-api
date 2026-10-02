-- ============================================================
-- V6__seed_test_data.sql
-- Muc dich: Du lieu test cho moi truong DEV/TEST.
-- KHONG dung cho PRODUCTION.
-- Chay lai an toan nho: INSERT ... ON CONFLICT DO NOTHING.
-- BCrypt hash cua Test@123:
--   $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Thu tu INSERT theo FK dependency (BLOCK 1 -> 14).
-- ============================================================


-- ============================================================
-- BLOCK 1: vai_tro
-- V2 da INSERT du 5 roles; block nay dam bao idempotent.
-- ============================================================
INSERT INTO vai_tro (id, ten_vai_tro, mo_ta)
OVERRIDING SYSTEM VALUE VALUES
  (1, 'ADMIN',    'Quan tri he thong'),
  (2, 'MANAGER',  'Quan ly cua hang'),
  (3, 'CASHIER',  'Thu ngan'),
  (4, 'BARISTA',  'Nhan vien pha che'),
  (5, 'CUSTOMER', 'Khach hang')
ON CONFLICT DO NOTHING;

SELECT setval('vai_tro_id_seq', (SELECT MAX(id) FROM vai_tro));


-- ============================================================
-- BLOCK 2: nguoi_dung — 4 nhan vien test (ngoai admin V2)
-- mat_khau_ma_hoa = BCrypt("Test@123")
-- ============================================================
INSERT INTO nguoi_dung (id, ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten, dang_hoat_dong)
OVERRIDING SYSTEM VALUE VALUES
  (2, 2, 'manager01', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nguyen Van Manager', TRUE),
  (3, 3, 'cashier01', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Tran Thi Cashier',   TRUE),
  (4, 4, 'barista01', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Le Van Barista',     TRUE),
  (5, 3, 'cashier02', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Pham Thi Thu Ngan',  TRUE)
ON CONFLICT DO NOTHING;

SELECT setval('nguoi_dung_id_seq', (SELECT MAX(id) FROM nguoi_dung));


-- ============================================================
-- BLOCK 3: loai_san_pham
-- V2 da co: id=1 (Tra sua), id=2 (Ca phe), id=3 (Da xay).
-- V6: them id=4 (Da xay moi), id=5 (Banh ngot);
--     doi id=3 thanh "Tra trai cay" de nhuong id=4 cho Da xay.
-- ============================================================
INSERT INTO loai_san_pham (id, ten_danh_muc)
OVERRIDING SYSTEM VALUE VALUES
  (1, 'Tra sua'),
  (2, 'Ca phe'),
  (3, 'Tra trai cay'),
  (4, 'Da xay'),
  (5, 'Banh ngot')
ON CONFLICT DO NOTHING;

-- Neu V2 da chay truoc (id=3 = 'Da xay'), doi ten thanh 'Tra trai cay'
UPDATE loai_san_pham
SET ten_danh_muc = 'Tra trai cay', updated_at = CURRENT_TIMESTAMP
WHERE id = 3 AND ten_danh_muc = 'Da xay';

SELECT setval('loai_san_pham_id_seq', (SELECT MAX(id) FROM loai_san_pham));


-- ============================================================
-- BLOCK 4: san_pham — 11 san pham trai deu 5 danh muc
-- ============================================================
INSERT INTO san_pham (id, ma_danh_muc, ten_san_pham, mo_ta)
OVERRIDING SYSTEM VALUE VALUES
  (1,  1, 'Tra sua tran chau', 'Tra sua classic voi tran chau den thom ngon'),
  (2,  1, 'Tra sua matcha',    'Tra sua matcha Nhat Ban beo thom'),
  (3,  1, 'Tra sua khoai mon', 'Tra sua khoai mon tim mong mo'),
  (4,  2, 'Ca phe sua da',     'Ca phe phin pha voi sua dac truyen thong'),
  (5,  2, 'Ca phe den',        'Ca phe den dam da, nguyen chat'),
  (6,  2, 'Bac xiu',           'Ca phe sua tuoi nhe nhang'),
  (7,  3, 'Tra dao cam sa',    'Tra trai cay thom mat voi dao, cam va sa'),
  (8,  3, 'Tra vai',           'Tra vai thieu tuoi thanh mat'),
  (9,  4, 'Da xay cookies',    'Da xay beo ngay voi banh cookies gion'),
  (10, 4, 'Da xay matcha',     'Da xay matcha mat lanh'),
  (11, 5, 'Banh flan',         'Banh flan caramel mem min thom ngon')
ON CONFLICT DO NOTHING;

SELECT setval('san_pham_id_seq', (SELECT MAX(id) FROM san_pham));


-- ============================================================
-- BLOCK 5: bien_the_san_pham — 3 bien the S/M/L cho moi san pham
-- Tong: 11 SP x 3 = 33 rows.
-- V5 da them cot: version BIGINT NOT NULL DEFAULT 0 (khong can khai bao).
-- so_luong_ton=100, con_hang=TRUE cho tat ca.
-- ============================================================
INSERT INTO bien_the_san_pham (id, ma_san_pham, kich_co, gia_ban, so_luong_ton, con_hang)
OVERRIDING SYSTEM VALUE VALUES
  -- SP 1: Tra sua tran chau
  (1,  1, 'S', 35000.00, 100, TRUE),
  (2,  1, 'M', 42500.00, 100, TRUE),
  (3,  1, 'L', 49000.00, 100, TRUE),
  -- SP 2: Tra sua matcha
  (4,  2, 'S', 38000.00, 100, TRUE),
  (5,  2, 'M', 45000.00, 100, TRUE),
  (6,  2, 'L', 52000.00, 100, TRUE),
  -- SP 3: Tra sua khoai mon
  (7,  3, 'S', 36000.00, 100, TRUE),
  (8,  3, 'M', 43000.00, 100, TRUE),
  (9,  3, 'L', 50000.00, 100, TRUE),
  -- SP 4: Ca phe sua da
  (10, 4, 'S', 29000.00, 100, TRUE),
  (11, 4, 'M', 35000.00, 100, TRUE),
  (12, 4, 'L', 40000.00, 100, TRUE),
  -- SP 5: Ca phe den
  (13, 5, 'S', 25000.00, 100, TRUE),
  (14, 5, 'M', 30000.00, 100, TRUE),
  (15, 5, 'L', 35000.00, 100, TRUE),
  -- SP 6: Bac xiu
  (16, 6, 'S', 30000.00, 100, TRUE),
  (17, 6, 'M', 36000.00, 100, TRUE),
  (18, 6, 'L', 42000.00, 100, TRUE),
  -- SP 7: Tra dao cam sa
  (19, 7, 'S', 35000.00, 100, TRUE),
  (20, 7, 'M', 42000.00, 100, TRUE),
  (21, 7, 'L', 48000.00, 100, TRUE),
  -- SP 8: Tra vai
  (22, 8, 'S', 33000.00, 100, TRUE),
  (23, 8, 'M', 40000.00, 100, TRUE),
  (24, 8, 'L', 46000.00, 100, TRUE),
  -- SP 9: Da xay cookies
  (25, 9, 'S', 45000.00, 100, TRUE),
  (26, 9, 'M', 52000.00, 100, TRUE),
  (27, 9, 'L', 59000.00, 100, TRUE),
  -- SP 10: Da xay matcha
  (28, 10, 'S', 43000.00, 100, TRUE),
  (29, 10, 'M', 50000.00, 100, TRUE),
  (30, 10, 'L', 57000.00, 100, TRUE),
  -- SP 11: Banh flan
  (31, 11, 'S', 20000.00, 100, TRUE),
  (32, 11, 'M', 25000.00, 100, TRUE),
  (33, 11, 'L', 30000.00, 100, TRUE)
ON CONFLICT DO NOTHING;

SELECT setval('bien_the_san_pham_id_seq', (SELECT MAX(id) FROM bien_the_san_pham));


-- ============================================================
-- BLOCK 6: topping — 5 topping mau
-- V5 da them cot: version BIGINT NOT NULL DEFAULT 0.
-- ============================================================
INSERT INTO topping (id, ten_topping, gia_ban, so_luong_ton, con_hang)
OVERRIDING SYSTEM VALUE VALUES
  (1, 'Tran chau den',   8000.00, 200, TRUE),
  (2, 'Thach dao',       8000.00, 150, TRUE),
  (3, 'Pudding trung',  10000.00, 100, TRUE),
  (4, 'Kem pho mai',    12000.00,  80, TRUE),
  (5, 'Tran chau trang', 8000.00, 120, TRUE)
ON CONFLICT DO NOTHING;

SELECT setval('topping_id_seq', (SELECT MAX(id) FROM topping));


-- ============================================================
-- BLOCK 7: khach_hang — 3 khach hang voi cac hang thanh vien
-- hang_thanh_vien enum: DONG | BAC | VANG | KIM_CUONG
-- ============================================================
INSERT INTO khach_hang (id, so_dien_thoai, ho_va_ten, email, diem_tich_luy, hang_thanh_vien, da_xac_thuc)
OVERRIDING SYSTEM VALUE VALUES
  (1, '0901234567', 'Nguyen Van A', 'a@test.com',  50, 'DONG', TRUE),
  (2, '0902345678', 'Tran Thi B',   'b@test.com', 250, 'BAC',  TRUE),
  (3, '0903456789', 'Le Van C',     'c@test.com', 750, 'VANG', TRUE)
ON CONFLICT DO NOTHING;

SELECT setval('khach_hang_id_seq', (SELECT MAX(id) FROM khach_hang));


-- ============================================================
-- BLOCK 8: ban — 10 ban voi QR token UUID duy nhat
-- trang_thai enum: TRONG | DANG_CO_KHACH | DA_DAT_TRUOC
-- ============================================================
INSERT INTO ban (id, so_ban, ma_qr_token, trang_thai)
OVERRIDING SYSTEM VALUE VALUES
  (1,  'Ban 01', 'a1b2c3d4-0001-4000-8000-000000000001', 'TRONG'),
  (2,  'Ban 02', 'a1b2c3d4-0002-4000-8000-000000000002', 'TRONG'),
  (3,  'Ban 03', 'a1b2c3d4-0003-4000-8000-000000000003', 'TRONG'),
  (4,  'Ban 04', 'a1b2c3d4-0004-4000-8000-000000000004', 'TRONG'),
  (5,  'Ban 05', 'a1b2c3d4-0005-4000-8000-000000000005', 'TRONG'),
  (6,  'Ban 06', 'a1b2c3d4-0006-4000-8000-000000000006', 'TRONG'),
  (7,  'Ban 07', 'a1b2c3d4-0007-4000-8000-000000000007', 'TRONG'),
  (8,  'Ban 08', 'a1b2c3d4-0008-4000-8000-000000000008', 'TRONG'),
  (9,  'Ban 09', 'a1b2c3d4-0009-4000-8000-000000000009', 'TRONG'),
  (10, 'Ban 10', 'a1b2c3d4-0010-4000-8000-000000000010', 'TRONG')
ON CONFLICT DO NOTHING;

SELECT setval('ban_id_seq', (SELECT MAX(id) FROM ban));


-- ============================================================
-- BLOCK 9: nha_cung_cap — 3 nha cung cap mau
-- ============================================================
INSERT INTO nha_cung_cap (id, ten_nha_cung_cap, so_dien_thoai, email, dia_chi, nguoi_dai_dien, dang_hop_tac)
OVERRIDING SYSTEM VALUE VALUES
  (1, 'Cong ty Nguyen Lieu A', '0281234567', 'contact@ncc-a.vn', '123 Le Loi, Q1, HCM',       'Nguyen Van X', TRUE),
  (2, 'Cong ty Tra B',         '0282345678', 'contact@ncc-b.vn', '456 Nguyen Hue, Q1, HCM',   'Tran Thi Y',   TRUE),
  (3, 'Cong ty Sua C',         '0283456789', 'contact@ncc-c.vn', '789 Hai Ba Trung, Q3, HCM', 'Le Van Z',     TRUE)
ON CONFLICT DO NOTHING;

SELECT setval('nha_cung_cap_id_seq', (SELECT MAX(id) FROM nha_cung_cap));


-- ============================================================
-- BLOCK 10: ca_lam_viec — 2 ca mau
-- Ca 1: cashier01 (nguoi_dung.id=3), trang_thai DA_DONG
--       tien_dau_ca=500,000  tien_ket_ca=1,500,000
-- Ca 2: cashier02 (nguoi_dung.id=5), trang_thai DANG_MO
--       tien_dau_ca=300,000  tien_ket_ca=NULL (chua dong)
-- ============================================================
INSERT INTO ca_lam_viec (id, ma_thu_ngan, thoi_gian_bat_dau, thoi_gian_ket_thuc, tien_dau_ca, tien_ket_ca, trang_thai)
OVERRIDING SYSTEM VALUE VALUES
  (1, 3,
   CURRENT_TIMESTAMP - INTERVAL '8 hours',
   CURRENT_TIMESTAMP - INTERVAL '1 hour',
   500000.00, 1500000.00, 'DA_DONG'),
  (2, 5,
   CURRENT_TIMESTAMP - INTERVAL '2 hours',
   NULL,
   300000.00, NULL, 'DANG_MO')
ON CONFLICT DO NOTHING;

SELECT setval('ca_lam_viec_id_seq', (SELECT MAX(id) FROM ca_lam_viec));


-- ============================================================
-- BLOCK 11: chuong_trinh_khuyen_mai — 3 chuong trinh
-- ============================================================
INSERT INTO chuong_trinh_khuyen_mai (id, ten_chuong_trinh, mo_ta, ngay_bat_dau, ngay_ket_thuc, dang_hoat_dong)
OVERRIDING SYSTEM VALUE VALUES
  (1, 'Khai truong 2026', 'Chuong trinh uu dai mung khai truong nam 2026',
   '2026-01-01 00:00:00+07', '2026-12-31 23:59:59+07', TRUE),
  (2, 'Happy Hour', 'Giam gia dac biet trong gio cao diem',
   '2026-01-01 00:00:00+07', '2026-12-31 23:59:59+07', TRUE),
  (3, 'Chao he 2026', 'Uu dai dac biet chao mung mua he 2026',
   '2026-06-01 00:00:00+07', '2026-08-31 23:59:59+07', FALSE)
ON CONFLICT DO NOTHING;

SELECT setval('chuong_trinh_khuyen_mai_id_seq', (SELECT MAX(id) FROM chuong_trinh_khuyen_mai));


-- ============================================================
-- BLOCK 12: khuyen_mai_voucher — 3 voucher thuoc chuong trinh id=1
-- loai_giam_gia: PHAN_TRAM | TIEN_CO_DINH
-- ============================================================
INSERT INTO khuyen_mai_voucher (id, ma_chuong_trinh, ma_code, loai_giam_gia, gia_tri_giam, muc_giam_toi_da, don_hang_toi_thieu, gioi_han_su_dung, so_luot_da_dung)
OVERRIDING SYSTEM VALUE VALUES
  (1, 1, 'CHAOMUNG20', 'PHAN_TRAM',    20.00,    50000.00, 100000.00, 100, 0),
  (2, 1, 'GIAM20K',    'TIEN_CO_DINH', 20000.00,     NULL,  50000.00, 500, 0),
  (3, 1, 'FREESHIP',   'TIEN_CO_DINH', 15000.00,     NULL,      0.00, 200, 0)
ON CONFLICT DO NOTHING;

SELECT setval('khuyen_mai_voucher_id_seq', (SELECT MAX(id) FROM khuyen_mai_voucher));


-- ============================================================
-- BLOCK 13: khuyen_mai_san_pham — Giam 10% cho danh muc Tra sua (id=1)
-- Thuoc chuong trinh "Khai truong 2026" (id=1).
-- CHECK: ma_san_pham IS NOT NULL OR ma_danh_muc IS NOT NULL
-- -> Dung ma_danh_muc=1, ma_san_pham=NULL.
-- ============================================================
INSERT INTO khuyen_mai_san_pham (id, ma_chuong_trinh, ma_san_pham, ma_danh_muc, loai_giam_gia, gia_tri_giam)
OVERRIDING SYSTEM VALUE VALUES
  (1, 1, NULL, 1, 'PHAN_TRAM', 10.00)
ON CONFLICT DO NOTHING;

SELECT setval('khuyen_mai_san_pham_id_seq', (SELECT MAX(id) FROM khuyen_mai_san_pham));


-- ============================================================
-- BLOCK 14: khuyen_mai_hoa_don — Giam 15% cho don tu 200,000, toi da 50,000
-- Thuoc chuong trinh "Happy Hour" (id=2).
-- ============================================================
INSERT INTO khuyen_mai_hoa_don (id, ma_chuong_trinh, don_hang_toi_thieu, loai_giam_gia, gia_tri_giam, muc_giam_toi_da)
OVERRIDING SYSTEM VALUE VALUES
  (1, 2, 200000.00, 'PHAN_TRAM', 15.00, 50000.00)
ON CONFLICT DO NOTHING;

SELECT setval('khuyen_mai_hoa_don_id_seq', (SELECT MAX(id) FROM khuyen_mai_hoa_don));


-- ============================================================
-- END OF V6__seed_test_data.sql
-- ============================================================
