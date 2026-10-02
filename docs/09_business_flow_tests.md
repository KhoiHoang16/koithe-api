# Testcase Business Flow - MilkTea Backend

> **Luu y kien truc:** Du an dung Application-Level Logic (Spring Boot Service layer).
> KHONG co database trigger. Tat ca nghiep vu xu ly o Service layer.
> DB chi enforce rang buoc tinh: NOT NULL, UNIQUE, FK, DEFAULT, CHECK.

---

## PHAN 1: KIEM TRA RANG BUOC TINH CUA DATABASE

> Cac test nay chay truc tiep bang SQL (psql hoac DBeaver) de xac minh DB enforce dung.

---

### 1.1. Rang buoc NOT NULL

**Bang nguoi_dung** (NOT NULL: ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)

```sql
-- NOT-NULL-01: ma_vai_tro
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (NULL, 'test_nn_role', 'hash', 'Test');
-- Ket qua mong doi: ERROR null value in column "ma_vai_tro"

-- NOT-NULL-02: ten_dang_nhap
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (1, NULL, 'hash', 'Test');
-- Ket qua mong doi: ERROR null value in column "ten_dang_nhap"

-- NOT-NULL-03: mat_khau_ma_hoa
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (1, 'test_nn_pw', NULL, 'Test');
-- Ket qua mong doi: ERROR null value in column "mat_khau_ma_hoa"

-- NOT-NULL-04: ho_va_ten
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (1, 'test_nn_name', 'hash', NULL);
-- Ket qua mong doi: ERROR null value in column "ho_va_ten"
```

**Bang san_pham** (NOT NULL: ma_danh_muc, ten_san_pham)

```sql
-- NOT-NULL-05
INSERT INTO san_pham (ma_danh_muc, ten_san_pham) VALUES (NULL, 'Test SP');
-- Ket qua mong doi: ERROR null value in column "ma_danh_muc"

-- NOT-NULL-06
INSERT INTO san_pham (ma_danh_muc, ten_san_pham) VALUES (1, NULL);
-- Ket qua mong doi: ERROR null value in column "ten_san_pham"
```

**Bang bien_the_san_pham** (NOT NULL: ma_san_pham, kich_co, gia_ban)

```sql
-- NOT-NULL-07: gia_ban
INSERT INTO bien_the_san_pham (ma_san_pham, kich_co, gia_ban, so_luong_ton)
VALUES (1, 'S', NULL, 100);
-- Ket qua mong doi: ERROR null value in column "gia_ban"

-- NOT-NULL-08: kich_co
INSERT INTO bien_the_san_pham (ma_san_pham, kich_co, gia_ban, so_luong_ton)
VALUES (1, NULL, 35000, 100);
-- Ket qua mong doi: ERROR null value in column "kich_co"
```

**Bang don_hang** (NOT NULL: ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu, tong_tien_hang, tong_tien_thanh_toan)

```sql
-- NOT-NULL-09: ma_hien_thi_don
INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu,
                      tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
VALUES (NULL, 'TAI_QUAY_POS', 'MANG_VE', 0, 0, 0);
-- Ket qua mong doi: ERROR null value in column "ma_hien_thi_don"

-- NOT-NULL-10: tong_tien_thanh_toan
INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu,
                      tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
VALUES ('DH-TEST-NN', 'TAI_QUAY_POS', 'MANG_VE', 100000, 0, NULL);
-- Ket qua mong doi: ERROR null value in column "tong_tien_thanh_toan"
```

**Bang ca_lam_viec** (NOT NULL: ma_thu_ngan)

```sql
-- NOT-NULL-11
INSERT INTO ca_lam_viec (ma_thu_ngan, tien_dau_ca) VALUES (NULL, 500000);
-- Ket qua mong doi: ERROR null value in column "ma_thu_ngan"
```

---

### 1.2. Rang buoc UNIQUE

```sql
-- UNIQUE-01: ten_dang_nhap (nguoi_dung)
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (1, 'admin', 'anyhash', 'Dup Admin');
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "nguoi_dung_ten_dang_nhap_key"

-- UNIQUE-02: so_dien_thoai (khach_hang)
INSERT INTO khach_hang (so_dien_thoai) VALUES ('0901234567');
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "khach_hang_so_dien_thoai_key"

-- UNIQUE-03: (ma_san_pham, kich_co) (bien_the_san_pham)
INSERT INTO bien_the_san_pham (ma_san_pham, kich_co, gia_ban, so_luong_ton)
VALUES (1, 'S', 99000.00, 50);
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "bien_the_san_pham_ma_san_pham_kich_co_key"

-- UNIQUE-04: ma_code (khuyen_mai_voucher)
INSERT INTO khuyen_mai_voucher
  (ma_chuong_trinh, ma_code, loai_giam_gia, gia_tri_giam, don_hang_toi_thieu)
VALUES (1, 'CHAOMUNG20', 'PHAN_TRAM', 10.00, 0.00);
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "khuyen_mai_voucher_ma_code_key"

-- UNIQUE-05: so_ban (ban)
INSERT INTO ban (so_ban, ma_qr_token) VALUES ('Ban 01', 'eeeeeeee-eeee-4eee-beee-eeeeeeeeeeee');
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "ban_so_ban_key"

-- UNIQUE-06: ma_qr_token (ban)
INSERT INTO ban (so_ban, ma_qr_token) VALUES ('Ban 99', 'a1b2c3d4-0001-4000-8000-000000000001');
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "ban_ma_qr_token_key"

-- UNIQUE-07: ten_vai_tro (vai_tro)
INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES ('ADMIN', 'Dup');
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "vai_tro_ten_vai_tro_key"

-- UNIQUE-08: ma_hien_thi_don (don_hang) -- gia su da co DH-2026-001
INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu,
                      tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
VALUES ('DH-2026-001', 'TAI_QUAY_POS', 'MANG_VE', 0, 0, 0);
-- Ket qua mong doi: ERROR duplicate key violates unique constraint "don_hang_ma_hien_thi_don_key"
```

---

### 1.3. Rang buoc FOREIGN KEY

**ON DELETE RESTRICT — khong cho xoa parent khi con con FK**

```sql
-- FK-01: Xoa vai_tro (CASHIER) dang duoc nguoi_dung dung
DELETE FROM vai_tro WHERE id = 3;
-- Mong doi: ERROR violates FK constraint on table "nguoi_dung"

-- FK-02: Insert san_pham voi ma_danh_muc khong ton tai
INSERT INTO san_pham (ma_danh_muc, ten_san_pham) VALUES (9999, 'SP Invalid');
-- Mong doi: ERROR Key (ma_danh_muc)=(9999) is not present in table "loai_san_pham"

-- FK-03: Insert bien_the voi ma_san_pham khong ton tai
INSERT INTO bien_the_san_pham (ma_san_pham, kich_co, gia_ban, so_luong_ton)
VALUES (9999, 'S', 35000, 100);
-- Mong doi: ERROR FK violation
```

**ON DELETE CASCADE — xoa theo**

```sql
-- FK-04: Xoa don_hang -> chi_tiet_don_hang cascade
BEGIN;
  INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu,
                        tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
  VALUES ('DH-CASCADE-TEST', 'TAI_QUAY_POS', 'MANG_VE', 0, 0, 0) RETURNING id;
  -- Ghi lai id -> <test_order_id>

  INSERT INTO chi_tiet_don_hang (ma_don_hang, ma_bien_the, so_luong, don_gia, thanh_tien)
  VALUES (<test_order_id>, 1, 1, 35000, 35000);

  SELECT COUNT(*) FROM chi_tiet_don_hang WHERE ma_don_hang = <test_order_id>;
  -- Mong doi: 1

  DELETE FROM don_hang WHERE ma_hien_thi_don = 'DH-CASCADE-TEST';

  SELECT COUNT(*) FROM chi_tiet_don_hang WHERE ma_don_hang = <test_order_id>;
  -- Mong doi: 0 (cascade delete)
ROLLBACK;

-- FK-05: Xoa chuong_trinh_khuyen_mai -> khuyen_mai_voucher cascade
BEGIN;
  DELETE FROM chuong_trinh_khuyen_mai WHERE id = 1;
  SELECT COUNT(*) FROM khuyen_mai_voucher WHERE ma_chuong_trinh = 1;
  -- Mong doi: 0 (3 voucher bi cascade delete)
ROLLBACK;
```

**ON DELETE SET NULL**

```sql
-- FK-06: Xoa ca_lam_viec -> don_hang.ma_ca set NULL
BEGIN;
  INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu, ma_ca,
                        tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
  VALUES ('DH-SETNULL-TEST', 'TAI_QUAY_POS', 'MANG_VE', 2, 0, 0, 0) RETURNING id;

  DELETE FROM ca_lam_viec WHERE id = 2;

  SELECT ma_ca FROM don_hang WHERE ma_hien_thi_don = 'DH-SETNULL-TEST';
  -- Mong doi: NULL (don van con, ca_id = NULL)
ROLLBACK;
```

---

### 1.4. Rang buoc DEFAULT

```sql
-- DEFAULT-01: nguoi_dung.dang_hoat_dong = TRUE
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES (5, 'test_default_u', 'hash', 'Test Default');
SELECT dang_hoat_dong, created_at, updated_at
FROM nguoi_dung WHERE ten_dang_nhap = 'test_default_u';
-- Mong doi: TRUE | NOT NULL | NOT NULL

-- DEFAULT-02: khach_hang defaults
INSERT INTO khach_hang (so_dien_thoai) VALUES ('0999000001');
SELECT hang_thanh_vien, diem_tich_luy, da_xac_thuc
FROM khach_hang WHERE so_dien_thoai = '0999000001';
-- Mong doi: DONG | 0 | FALSE

-- DEFAULT-03: don_hang.trang_thai = 'CHO_XAC_NHAN'
INSERT INTO don_hang (ma_hien_thi_don, kenh_dat_hang, loai_phuc_vu,
                      tong_tien_hang, so_tien_giam, tong_tien_thanh_toan)
VALUES ('DH-DEF-TEST', 'TAI_QUAY_POS', 'MANG_VE', 0, 0, 0);
SELECT trang_thai FROM don_hang WHERE ma_hien_thi_don = 'DH-DEF-TEST';
-- Mong doi: 'CHO_XAC_NHAN'

-- DEFAULT-04: ban.trang_thai = 'TRONG'
INSERT INTO ban (so_ban, ma_qr_token) VALUES ('Ban Test', 'ffffffff-ffff-4fff-bfff-ffffffffffff');
SELECT trang_thai FROM ban WHERE so_ban = 'Ban Test';
-- Mong doi: 'TRONG'

-- DEFAULT-05: bien_the_san_pham: so_luong_ton=0, con_hang=TRUE, version=0 (V5)
INSERT INTO bien_the_san_pham (ma_san_pham, kich_co, gia_ban)
VALUES (1, 'XL', 55000.00);
SELECT so_luong_ton, con_hang, version
FROM bien_the_san_pham WHERE ma_san_pham = 1 AND kich_co = 'XL';
-- Mong doi: 0 | TRUE | 0

-- DEFAULT-06: ca_lam_viec.trang_thai = 'DANG_MO', tien_dau_ca = 0
INSERT INTO ca_lam_viec (ma_thu_ngan) VALUES (3);
SELECT trang_thai, tien_dau_ca FROM ca_lam_viec ORDER BY id DESC LIMIT 1;
-- Mong doi: 'DANG_MO' | 0.00

-- Cleanup
DELETE FROM nguoi_dung WHERE ten_dang_nhap = 'test_default_u';
DELETE FROM khach_hang WHERE so_dien_thoai = '0999000001';
DELETE FROM don_hang WHERE ma_hien_thi_don = 'DH-DEF-TEST';
DELETE FROM ban WHERE so_ban = 'Ban Test';
DELETE FROM bien_the_san_pham WHERE ma_san_pham = 1 AND kich_co = 'XL';
```

---

### 1.5. Rang buoc CHECK

```sql
-- CHECK-01: chi_tiet_don_hang.so_luong > 0
INSERT INTO chi_tiet_don_hang (ma_don_hang, ma_bien_the, so_luong, don_gia, thanh_tien)
VALUES (1, 1, 0, 35000, 0);
-- Mong doi: ERROR violates check constraint "chi_tiet_don_hang_so_luong_check"

-- CHECK-02: chi_tiet_gio_hang.so_luong > 0
INSERT INTO chi_tiet_gio_hang (ma_gio_hang, ma_bien_the, so_luong, thanh_tien)
VALUES (1, 1, -1, 0);
-- Mong doi: ERROR violates check constraint "chi_tiet_gio_hang_so_luong_check"

-- CHECK-03: khuyen_mai_san_pham: ca ma_san_pham va ma_danh_muc deu NULL
INSERT INTO khuyen_mai_san_pham
  (ma_chuong_trinh, ma_san_pham, ma_danh_muc, loai_giam_gia, gia_tri_giam)
VALUES (1, NULL, NULL, 'PHAN_TRAM', 10.00);
-- Mong doi: ERROR violates check constraint "khuyen_mai_san_pham_check"

-- CHECK-04: chi_tiet_phieu_nhap: ca ma_bien_the va ma_topping deu NULL
INSERT INTO chi_tiet_phieu_nhap
  (ma_phieu_nhap, ma_bien_the, ma_topping, ten_mat_hang, don_vi_tinh, so_luong, don_gia_nhap, thanh_tien)
VALUES (1, NULL, NULL, 'Test hang', 'kg', 1, 50000, 50000);
-- Mong doi: ERROR violates check constraint "chi_tiet_phieu_nhap_check"

-- CHECK-05: order_sequence.last_value >= 0
INSERT INTO order_sequence (nam, last_value) VALUES (2025, -1);
-- Mong doi: ERROR violates check constraint "order_sequence_last_value_check"
```

---

### 1.6. Nghiep vu KHONG duoc DB enforce — phai test qua Service layer

| Nghiep vu | Enforce o dau | File / Dong code | Cach test |
|:---|:---|:---|:---|
| Tru ton kho khi DA_THANH_TOAN | OrderServiceImpl#changeInventory() | OrderServiceImpl.java:212 | Phan 3.2 |
| Hoan ton kho khi DA_HUY (ton_kho_da_tru=true) | OrderServiceImpl#changeInventory(restore) | OrderServiceImpl.java:213 | Phan 3.2 |
| Sinh ma don DH-YYYY-NNN (atomic) | OrderNumberGenerator#next() | OrderNumberGenerator.java:14 | Phan 3.5 |
| Snapshot gia bien the | OrderServiceImpl#addSnapshotLine() | OrderServiceImpl.java:182 | Phan 3.1 |
| Snapshot gia topping | OrderServiceImpl#addSnapshotLine() | OrderServiceImpl.java:190 | Phan 3.1 |
| State machine don hang | OrderServiceImpl#transition() | OrderServiceImpl.java:203-214 | Phan 3.3 |
| Merge guest cart khi dang nhap | CartServiceImpl#resolve() | CartServiceImpl.java:122-127 | Phan 3.4 |
| Kiem tra ton kho truoc add cart | CartServiceImpl#requireStock() | CartServiceImpl.java:164-166 | CART-04 |
| Ca phai DANG_MO va thuoc cashier | OrderServiceImpl#createPos() | OrderServiceImpl.java:118 | ORD-05/06 |
| Customer phai co so dien thoai | CartServiceImpl#customer() | CartServiceImpl.java:142-143 | Dang nhap khong SDT |
| Cap nhat updated_at | Hibernate @UpdateTimestamp | BaseEntity.java:14 | Phan 3.7 |

---

## PHAN 2: KIEM TRA API + SERVICE LOGIC

> Base URL: http://localhost:8080
> Seed data V6: admin/Test@123, manager01/Test@123, cashier01/Test@123, barista01/Test@123.

---

### 2.1. Module Auth — 10 test cases

| TC# | Ten test | Method + URL | Body / Ghi chu | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|
| AUTH-01 | Dang nhap thanh cong (admin) | POST /api/auth/login | {"username":"admin","password":"Test@123"} | accessToken + refreshToken | 200 |
| AUTH-02 | Dang nhap sai mat khau | POST /api/auth/login | {"username":"admin","password":"SaiMatKhau"} | error message | 401 |
| AUTH-03 | Username khong ton tai | POST /api/auth/login | {"username":"khongtontai","password":"Test@123"} | error message | 401 |
| AUTH-04 | Dang ky CUSTOMER moi | POST /api/auth/register | {"username":"newcustomer","password":"Test@123","fullName":"KH Moi"} | token, role=CUSTOMER | 201 |
| AUTH-05 | Dang ky trung username | POST /api/auth/register | {"username":"admin","password":"Test@123","fullName":"Dup"} | error da ton tai | 409 |
| AUTH-06 | Lay /me co token | GET /api/auth/me | Authorization: Bearer <token> | id, username, role | 200 |
| AUTH-07 | Lay /me khong token | GET /api/auth/me | (khong header) | 401 Unauthorized | 401 |
| AUTH-08 | Refresh token hop le | POST /api/auth/refresh | {"refreshToken":"<valid>"} | accessToken moi | 200 |
| AUTH-09 | Refresh token sai | POST /api/auth/refresh | {"refreshToken":"invalid"} | 401/400 | 401 |
| AUTH-10 | Dang xuat | POST /api/auth/logout | {"refreshToken":"<valid>"} | 204 No Content | 204 |

---

### 2.2. Module Catalog — 13 test cases

| TC# | Ten test | Method + URL | Auth | Param / Body | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|:---|
| CAT-01 | Danh sach danh muc | GET /api/categories | Anonymous | - | 5 danh muc | 200 |
| CAT-02 | Tao danh muc moi | POST /api/categories | ADMIN/MANAGER | {"tenDanhMuc":"Nuoc ep"} | Tao thanh cong | 201 |
| CAT-03 | Tao danh muc (CASHIER) | POST /api/categories | CASHIER | {"tenDanhMuc":"Test"} | 403 Forbidden | 403 |
| CAT-04 | SP phan trang | GET /api/products?page=0&size=5 | Anonymous | - | 5 SP, totalElements=11 | 200 |
| CAT-05 | Loc SP theo danh muc | GET /api/products?categoryId=1 | Anonymous | - | 3 SP Tra sua | 200 |
| CAT-06 | Tim SP theo keyword | GET /api/products?keyword=matcha | Anonymous | - | 2 SP co "matcha" | 200 |
| CAT-07 | Chi tiet SP | GET /api/products/1 | Anonymous | - | Chi tiet SP id=1 | 200 |
| CAT-08 | SP khong ton tai | GET /api/products/9999 | Anonymous | - | 404 Not Found | 404 |
| CAT-09 | Tao SP moi | POST /api/products | ADMIN | {"maDanhMuc":1,"tenSanPham":"Tra xanh"} | SP moi | 201 |
| CAT-10 | Bien the cua SP | GET /api/products/1/variants | Anonymous | - | 3 bien the S/M/L | 200 |
| CAT-11 | Them bien the moi | POST /api/products/1/variants | ADMIN/MANAGER | {"kichCo":"XL","giaBan":60000,"soLuongTon":50} | Bien the XL | 201 |
| CAT-12 | Danh sach topping | GET /api/toppings | Anonymous | - | 5 topping | 200 |
| CAT-13 | Xoa danh muc con SP | DELETE /api/categories/1 | ADMIN | - | 409/400 con san pham | 409 |

---

### 2.3. Module Cart — 10 test cases

> CartController dung header X-Cart-Token (UUID) dinh danh guest.
> CartServiceImpl#resolve() tu merge guest cart khi CUSTOMER dang nhap (dong 122-127).

| TC# | Ten test | Method + URL | Auth / Header | Body | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|:---|
| CART-01 | Them mon guest (khong token) | POST /api/carts/items | Anonymous | {"variantId":1,"quantity":2} | 201, response co X-Cart-Token moi | 201 |
| CART-02 | Them mon (dung lai token) | POST /api/carts/items | X-Cart-Token: <CART-01-token> | {"variantId":2,"quantity":1} | 201, gio co 2 mon | 201 |
| CART-03 | Xem gio hang hien tai | GET /api/carts/current | X-Cart-Token: <token> | - | Gio voi cac mon | 200 |
| CART-04 | Them mon het hang | POST /api/carts/items | Anonymous | {"variantId":1,"quantity":9999} | 409 khong du ton kho | 409 |
| CART-05 | Them mon co topping | POST /api/carts/items | X-Cart-Token: <token> | {"variantId":1,"quantity":1,"toppings":[{"toppingId":1,"quantity":1}]} | 201, co topping | 201 |
| CART-06 | Sua so luong mon | PATCH /api/carts/items/{id} | X-Cart-Token: <token> | {"quantity":3} | 200, thanh_tien tinh lai | 200 |
| CART-07 | Xoa mot mon | DELETE /api/carts/items/{id} | X-Cart-Token: <token> | - | 204 | 204 |
| CART-08 | Xoa toan bo gio | DELETE /api/carts/current | X-Cart-Token: <token> | - | 204, gio trong | 204 |
| CART-09 | Merge guest cart khi dang nhap | POST /api/carts/items | JWT CUSTOMER + X-Cart-Token: <guest-token> | {"variantId":3,"quantity":1} | Guest items merge vao customer cart | 201 |
| CART-10 | Token sai dinh dang | POST /api/carts/items | X-Cart-Token: "not-a-uuid" | {"variantId":1,"quantity":1} | 400 token khong hop le | 400 |

---

### 2.4. Module Order — 16 test cases

#### Nhom A: Tao don hang

| TC# | Ten test | Method + URL | Auth | Body / Ghi chu | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|:---|
| ORD-01 | Tao don tu gio hang (CUSTOMER) | POST /api/orders | CUSTOMER | {"cartToken":"<token>","serviceType":"MANG_VE","channel":"QUET_QR_BAN"} | 201, CHO_XAC_NHAN, ma=DH-2026-XXX | 201 |
| ORD-02 | Tao don POS (CASHIER) | POST /api/orders/pos | CASHIER | {"cashierId":3,"shiftId":2,"serviceType":"MANG_VE","items":[{"variantId":1,"quantity":2}]} | 201 | 201 |
| ORD-03 | Don POS TAI_CHO co ban | POST /api/orders/pos | CASHIER | {...,"serviceType":"TAI_CHO","tableId":1,...} | 201, ma_ban=1 | 201 |
| ORD-04 | TAI_CHO thieu tableId | POST /api/orders/pos | CASHIER | {...,"serviceType":"TAI_CHO"} thieu tableId | 400 phai chon ban | 400 |
| ORD-05 | Ca DA_DONG (shiftId=1) | POST /api/orders/pos | CASHIER | {"cashierId":3,"shiftId":1,...} | 400 ca khong mo | 400 |
| ORD-06 | Cashier khong thuoc ca | POST /api/orders/pos | CASHIER | {"cashierId":5,"shiftId":2,...} | 400 ca khong thuoc cashier | 400 |
| ORD-07 | Gio hang rong | POST /api/orders | CUSTOMER | {"cartToken":"<empty>","serviceType":"MANG_VE",...} | 400 gio rong | 400 |
| ORD-08 | Bien the het hang | POST /api/orders/pos | CASHIER | items voi variant het hang | 409 ton kho | 409 |

#### Nhom B: State Machine

| TC# | Ten test | Method + URL | Auth | Body | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|:---|
| ORD-09 | CHO_XAC_NHAN -> DA_THANH_TOAN | PATCH /api/orders/{id}/status | CASHIER | {"status":"DA_THANH_TOAN"} | 200, ton kho bi tru | 200 |
| ORD-10 | DA_THANH_TOAN -> DANG_PHA_CHE | PATCH /api/orders/{id}/status | BARISTA | {"status":"DANG_PHA_CHE"} | 200 | 200 |
| ORD-11 | DANG_PHA_CHE -> SAN_SANG | PATCH /api/orders/{id}/status | BARISTA | {"status":"SAN_SANG"} | 200 | 200 |
| ORD-12 | SAN_SANG -> HOAN_THANH | PATCH /api/orders/{id}/status | CASHIER | {"status":"HOAN_THANH"} | 200 | 200 |
| ORD-13 | HOAN_THANH -> bat ky (invalid) | PATCH /api/orders/{id}/status | ADMIN | {"status":"DA_THANH_TOAN"} | 400 khong the chuyen | 400 |
| ORD-14 | CHO_XAC_NHAN -> DANG_PHA_CHE (skip) | PATCH /api/orders/{id}/status | CASHIER | {"status":"DANG_PHA_CHE"} | 400 sai buoc | 400 |

#### Nhom C: Huy don

| TC# | Ten test | Method + URL | Auth | Ghi chu | Ket qua mong doi | HTTP |
|:---|:---|:---|:---|:---|:---|:---|
| ORD-15 | Huy don CHO_XAC_NHAN | POST /api/orders/{id}/cancel | ADMIN | Chua tru ton kho | 200, DA_HUY, ton_kho_da_tru=FALSE | 200 |
| ORD-16 | Huy don DA_THANH_TOAN (hoan ton kho) | POST /api/orders/{id}/cancel | ADMIN | Da tru ton kho | 200, DA_HUY, ton kho phuc hoi | 200 |

---

## PHAN 3: KIEM TRA BUSINESS RULES QUA SERVICE LAYER

---

### 3.1. Snapshot gia — don cu khong doi khi catalog thay doi

**Tham chieu code:** OrderServiceImpl#addSnapshotLine() dong 182: `line.setDonGia(variant.getGiaBan())`

```
B1: GET /api/products/1/variants -> S = 35,000

B2: Tao don
    POST /api/orders/pos
    {"cashierId":3,"shiftId":2,"serviceType":"MANG_VE",
     "items":[{"variantId":1,"quantity":2}]}
    -> don_id = <X>

B3: Verify snapshot
    SELECT don_gia, thanh_tien FROM chi_tiet_don_hang WHERE ma_don_hang = <X>;
    -- Mong doi: 35000.00 | 70000.00

B4: Doi gia catalog
    PUT /api/products/1/variants/1
    {"kichCo":"S","giaBan":99000,"soLuongTon":100}

B5: Verify don cu KHONG doi
    SELECT don_gia FROM chi_tiet_don_hang WHERE ma_don_hang = <X>;
    -- Mong doi: VAN = 35000.00

B6: Tao don moi -> don_gia = 99000.00
```

---

### 3.2. Tru / hoan ton kho — verify SQL

**Tham chieu code:** OrderServiceImpl#changeInventory() dong 217-237

```
B1: Ghi nhan ton kho
    SELECT so_luong_ton FROM bien_the_san_pham WHERE id=2; -- 100
    SELECT so_luong_ton FROM topping WHERE id=1;           -- 200

B2: Tao don: variant id=2 qty=3, topping id=1 qty=1
    POST /api/orders/pos {...}

B3: PATCH DA_THANH_TOAN (bat changeInventory)
    PATCH /api/orders/{id}/status {"status":"DA_THANH_TOAN"}

B4: Verify tru
    SELECT so_luong_ton FROM bien_the_san_pham WHERE id=2;
    -- Mong doi: 97  (100 - 3)

    SELECT so_luong_ton FROM topping WHERE id=1;
    -- Mong doi: 197  (200 - 3*1 = 3)

    SELECT ton_kho_da_tru FROM don_hang WHERE id=<id>;
    -- Mong doi: TRUE

B5: Huy don
    POST /api/orders/{id}/cancel

B6: Verify hoan ton kho
    SELECT so_luong_ton FROM bien_the_san_pham WHERE id=2; -- 100
    SELECT so_luong_ton FROM topping WHERE id=1;           -- 200
    SELECT ton_kho_da_tru FROM don_hang WHERE id=<id>;     -- FALSE
```

---

### 3.3. State machine — bang kiem tra toan bo transition

**Tham chieu code:** OrderServiceImpl#transition() dong 203-214

| Tu trang thai  | Sang trang thai  | HTTP  | Ghi chu                             |
|:---------------|:-----------------|:------|:------------------------------------|
| CHO_XAC_NHAN  | DA_THANH_TOAN    | 200   | Tru ton kho, ton_kho_da_tru=TRUE    |
| CHO_XAC_NHAN  | DA_HUY           | 200   | Chua tru, khong hoan                |
| CHO_XAC_NHAN  | DANG_PHA_CHE     | 400   | Skip buoc DA_THANH_TOAN — sai       |
| CHO_XAC_NHAN  | SAN_SANG         | 400   | Sai transition                      |
| CHO_XAC_NHAN  | HOAN_THANH       | 400   | Sai transition                      |
| DA_THANH_TOAN | DANG_PHA_CHE     | 200   | Hop le                              |
| DA_THANH_TOAN | CHO_XAC_NHAN     | 400   | Khong the quay lui                  |
| DA_THANH_TOAN | DA_HUY           | 200   | Hoan ton kho, ton_kho_da_tru=FALSE  |
| DANG_PHA_CHE  | SAN_SANG         | 200   | Hop le                              |
| DANG_PHA_CHE  | DA_THANH_TOAN    | 400   | Khong the quay lui                  |
| DANG_PHA_CHE  | DA_HUY           | 200   | Hoan ton kho                        |
| SAN_SANG      | HOAN_THANH       | 200   | Trang thai cuoi thanh cong          |
| SAN_SANG      | DA_HUY           | 200   | Hoan ton kho                        |
| SAN_SANG      | DANG_PHA_CHE     | 400   | Khong the quay lui                  |
| HOAN_THANH    | (bat ky)         | 400   | Trang thai cuoi — default Set.of()  |
| DA_HUY        | (bat ky)         | 400   | Trang thai cuoi — default Set.of()  |

---

### 3.4. Merge Guest Cart — Test end-to-end

**Tham chieu code:** CartServiceImpl#resolve() dong 122-127

```
B1: Tao gio guest (khong JWT, khong token)
    POST /api/carts/items {"variantId":4,"quantity":2}
    -> Lay X-Cart-Token tu response header: <GUEST_TOKEN>

B2: Them mon thu 2 vao guest
    POST /api/carts/items (X-Cart-Token: <GUEST_TOKEN>)
    {"variantId":5,"quantity":1}

B3: Xac nhan gio guest co 2 mon
    GET /api/carts/current (X-Cart-Token: <GUEST_TOKEN>)

B4: Dang nhap CUSTOMER
    POST /api/auth/login {"username":"newcustomer","password":"Test@123"}
    -> <ACCESS_TOKEN>

B5: Them mon voi ca JWT + guest token (bat merge)
    POST /api/carts/items
    Header: Authorization: Bearer <ACCESS_TOKEN>
    Header: X-Cart-Token: <GUEST_TOKEN>
    Body: {"variantId":6,"quantity":1}

B6: Verify merge
    GET /api/carts/current (Authorization: Bearer <ACCESS_TOKEN>)
    -> Mong doi: 3 items (2 tu guest + 1 moi)

B7: Verify guest cart soft-deleted
    SELECT deleted_at FROM gio_hang WHERE token_phien = '<GUEST_TOKEN>';
    -> Mong doi: deleted_at IS NOT NULL
```

---

### 3.5. Sinh ma don concurrent — Khong trung lap

**Tham chieu code:** OrderNumberGenerator.java dong 14, bang order_sequence (V5)
**Format:** `DH-{YYYY}-{NNN}` (3 chu so, zero-padded)

```bash
# Chay 20 request dong thoi (bash/WSL)
for i in $(seq 1 20); do
  curl -s -X POST http://localhost:8080/api/orders/pos \
    -H "Authorization: Bearer <CASHIER_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{"cashierId":3,"shiftId":2,"serviceType":"MANG_VE","items":[{"variantId":1,"quantity":1}]}' &
done
wait

# Kiem tra khong co ma trung
SELECT ma_hien_thi_don, COUNT(*) cnt
FROM don_hang WHERE ma_hien_thi_don LIKE 'DH-2026-%'
GROUP BY ma_hien_thi_don HAVING COUNT(*) > 1;
-- Mong doi: 0 rows

# Kiem tra counter
SELECT * FROM order_sequence WHERE nam = 2026;
-- Mong doi: last_value = so don da tao
```

---

### 3.6. Optimistic Locking — Ngan Race Condition

**Tham chieu:** BienTheSanPham.version + Topping.version (them tai V5)

```
B1: Set ton kho thap
    UPDATE bien_the_san_pham SET so_luong_ton=1, con_hang=TRUE WHERE id=1;

B2: Tao 2 don hang (don A, don B) voi cung variant id=1 qty=1

B3: Goi dong thoi 2 PATCH DA_THANH_TOAN
    curl ... <A>/status -d '{"status":"DA_THANH_TOAN"}' &
    curl ... <B>/status -d '{"status":"DA_THANH_TOAN"}' &
    wait

B4: Xac nhan
    1 request -> 200 (thanh cong)
    1 request -> 409 (Conflict: khong du ton kho)

    SELECT so_luong_ton, con_hang FROM bien_the_san_pham WHERE id=1;
    -- Mong doi: 0 | FALSE  (KHONG am)
```

---

### 3.7. Audit updated_at — JPA @UpdateTimestamp tu dong

**Tham chieu code:** BaseEntity.java dong 14:
`@UpdateTimestamp @Column(name="updated_at") private Instant updatedAt;`
Hibernate tu cap nhat moi khi entity duoc flush. KHONG can DB trigger.

```sql
-- B1: Ghi nhan updated_at truoc
SELECT id, ten_san_pham, updated_at, created_at FROM san_pham WHERE id=1;
-- Vi du: updated_at = 2026-10-02 10:00:00+07

-- B2: Goi API cap nhat (ADMIN)
-- PUT /api/products/1
-- Body: {"maDanhMuc":1,"tenSanPham":"Tra sua tran chau UPDATED","moTa":"..."}

-- B3: Verify updated_at da tang
SELECT id, ten_san_pham, updated_at, created_at FROM san_pham WHERE id=1;
-- Mong doi:
--   updated_at > 2026-10-02 10:00:00+07  (thoi diem moi)
--   created_at = gia tri goc             (KHONG doi, updatable=false)
```

| Entity | API cap nhat | Cot verify |
|:---|:---|:---|
| SanPham | PUT /api/products/{id} | san_pham.updated_at |
| BienTheSanPham | PUT /api/variants/{id} | bien_the_san_pham.updated_at |
| Topping | PUT /api/toppings/{id} | topping.updated_at |
| DonHang | PATCH /api/orders/{id}/status | don_hang.updated_at |
| KhachHang | PUT /api/customers/{id} | khach_hang.updated_at |

---

## PHAN 4: MATRIX PHAN QUYEN (RBAC)

> Test voi token tuong ung: admin, manager01, cashier01, barista01, newcustomer.

| Endpoint | ADMIN | MANAGER | CASHIER | BARISTA | CUSTOMER | Anonymous |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| POST /api/categories | 201 | 201 | 403 | 403 | 403 | 401 |
| POST /api/products | 201 | 201 | 403 | 403 | 403 | 401 |
| PUT /api/products/{id} | 200 | 200 | 403 | 403 | 403 | 401 |
| DELETE /api/products/{id} | 204 | 403 | 403 | 403 | 403 | 401 |
| POST /api/orders/pos | 201 | 201 | 201 | 403 | 403 | 401 |
| PATCH /api/orders/{id}/status | 200 | 200 | 200 | 200 | 403 | 401 |
| POST /api/orders/{id}/cancel | 200 | 200 | 403 | 403 | 200* | 401 |
| GET /api/orders (list all) | 200 | 200 | 200 | 403 | 403 | 401 |
| GET /api/orders/my | 403 | 403 | 403 | 403 | 200 | 401 |
| GET /api/products (list) | 200 | 200 | 200 | 200 | 200 | 200 |

> (*) CUSTOMER chi huy duoc don cua chinh minh (OrderServiceImpl#authorizeOwnerOrStaff dong 282).

---

## PHAN 5: CHECKLIST TRUOC KHI RELEASE

```
[ ] NOT NULL (11 test): tat ca deu bao ERROR dung
[ ] UNIQUE (8 test): tat ca deu bao duplicate error
[ ] FK RESTRICT: ngan xoa parent khi con con tham chieu
[ ] FK CASCADE: xoa theo dung (don_hang, khuyen_mai)
[ ] FK SET NULL: set null dung (don_hang.ma_ca)
[ ] DEFAULT (6 test): tat ca gia tri mac dinh dung
[ ] CHECK (5 test): tat ca deu bat vi pham
[ ] AUTH-01 den AUTH-10: pass
[ ] CAT-01 den CAT-13: pass
[ ] CART-01 den CART-10: pass
[ ] ORD-01 den ORD-16: pass
[ ] Snapshot gia: don cu khong bi anh huong khi doi gia catalog
[ ] Tru ton kho: giam dung khi DA_THANH_TOAN
[ ] Hoan ton kho: phuc hoi khi DA_HUY co ton_kho_da_tru=TRUE
[ ] State machine: HOAN_THANH/DA_HUY khong chuyen duoc nua
[ ] Merge cart: guest items merge vao customer cart, guest bi soft-delete
[ ] Concurrent order: 20 don dong thoi khong co ma trung
[ ] Optimistic lock: 1 trong 2 request phai that bai (409)
[ ] updated_at: tu cap nhat sau PUT/PATCH (khong can trigger)
[ ] RBAC: sai role bi chan 403, chua login bi chan 401
```
