# 📘 CHỨC NĂNG & LUỒNG HOẠT ĐỘNG — DỰ ÁN MILKTEA

> Tài liệu mô tả các chức năng của hệ thống bán trà sữa MilkTea và luồng hoạt động của từng chức năng.
> **Phiên bản:** 1.0 | **Ngày:** 09/10/2026

---

## 📖 MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Danh sách chức năng theo Role](#2-danh-sách-chức-năng-theo-role)
3. [Luồng hoạt động chi tiết](#3-luồng-hoạt-động-chi-tiết)

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1. Mục tiêu
Hệ thống MilkTea là nền tảng **POS & Omnichannel Ordering** cho chuỗi trà sữa, hỗ trợ 3 kênh bán hàng:
- **Tại quầy (POS):** Thu ngân tạo đơn trực tiếp.
- **Quét QR tại bàn:** Khách tự gọi món tại chỗ.
- **Online (Web/App):** Khách đặt hàng từ xa.

### 1.2. Sơ đồ kiến trúc tổng quan

```
┌──────────────────────────────────────────────────┐
│              CLIENT LAYER                        │
│  ┌──────────┐  ┌────────────┐  ┌──────────┐    │
│  │Customer  │  │Admin       │  │POS       │    │
│  │Web       │  │Dashboard   │  │(ngoài)   │    │
│  └────┬─────┘  └─────┬──────┘  └────┬─────┘    │
└───────┼──────────────┼──────────────┼──────────┘
        │              │              │
        └──────────────┼──────────────┘
                       │ HTTPS/JSON
                       ▼
        ┌──────────────────────────────┐
        │    Spring Boot Backend       │
        │    (Modular Monolith)        │
        └──────────┬───────────────────┘
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   ┌─────────┐ ┌────────┐ ┌────────┐
   │PostgreSQL│ │ Redis  │ │VNPay/  │
   │   16     │ │   7    │ │ Momo   │
   └─────────┘ └────────┘ └────────┘
```

### 1.3. Danh sách Actor

| Actor | Mô tả | Quyền hạn |
|:---|:---|:---|
| **GUEST** | Khách vãng lai, chưa đăng nhập | Xem menu, đặt hàng, tra cứu đơn |
| **CUSTOMER** | Khách có tài khoản | Đặt hàng, tích điểm, xem lịch sử |
| **CASHIER** | Thu ngân | Mở/đóng ca, tạo đơn POS |
| **BARISTA** | Pha chế | Xem order queue, cập nhật trạng thái |
| **MANAGER** | Quản lý cửa hàng | CRUD thực đơn, kho, xem báo cáo |
| **ADMIN** | Quản trị viên | Toàn quyền hệ thống |

---

## 2. DANH SÁCH CHỨC NĂNG THEO ROLE

### 2.1. Bảng tổng hợp

| Module | GUEST | CUSTOMER | CASHIER | BARISTA | MANAGER | ADMIN |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| **auth** | Đăng nhập, Đăng ký | Đăng nhập, Logout | Đăng nhập | Đăng nhập | Đăng nhập | Đăng nhập |
| **catalog** | Xem menu | Xem menu | Xem menu | Xem menu | CRUD thực đơn | CRUD thực đơn |
| **cart** | Thêm/sửa/xóa giỏ | Thêm/sửa/xóa giỏ, Merge | — | — | — | — |
| **order** | Tạo đơn, Tra cứu | Tạo đơn, Lịch sử, Hủy | Tạo đơn POS, Cập nhật | Cập nhật trạng thái | Quản lý đơn | Quản lý đơn |
| **payment** | — | Thanh toán | Thanh toán | — | — | — |
| **promotion** | Áp voucher | Áp voucher | Áp voucher | — | CRUD KM | CRUD KM |
| **shift** | — | — | Mở/Đóng ca | — | Xem lịch sử | Xem lịch sử |
| **table** | Xác thực QR | Xác thực QR | Đổi trạng thái | — | CRUD bàn | CRUD bàn |
| **customer** | — | Xem/Sửa profile | — | — | Quản lý KH | Quản lý KH |
| **inventory** | — | — | — | — | CRUD NCC, Nhập kho | CRUD NCC, Nhập kho |
| **report** | — | — | — | — | Xem báo cáo | Xem báo cáo |
| **user** | — | — | — | — | — | CRUD nhân viên |

### 2.2. Chi tiết chức năng theo Role

#### 🟢 GUEST — Khách vãng lai
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Đăng nhập | Đăng nhập bằng SĐT + password |
| 2 | Đăng ký | Tạo tài khoản CUSTOMER bằng SĐT |
| 3 | Xem danh mục | Xem danh sách loại sản phẩm |
| 4 | Xem thực đơn | Xem danh sách sản phẩm với ảnh, giá |
| 5 | Xem chi tiết sản phẩm | Xem biến thể S/M/L, topping |
| 6 | Thêm vào giỏ | Thêm món với tùy chọn size/đường/đá/topping |
| 7 | Quản lý giỏ | Sửa/xóa món trong giỏ |
| 8 | Đặt hàng | Tạo đơn hàng không cần đăng nhập |
| 9 | Tra cứu đơn | Tra cứu bằng mã đơn + SĐT |
| 10 | Quét QR bàn | Quét QR để gọi món tại chỗ |

#### 🔵 CUSTOMER — Khách hàng thành viên
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Tất cả chức năng của Guest | — |
| 2 | Merge giỏ hàng | Merge giỏ guest khi login |
| 3 | Thanh toán online | VNPay, Momo, QR chuyển khoản |
| 4 | Xem lịch sử đơn | Danh sách đơn của mình |
| 5 | Theo dõi đơn | Real-time status |
| 6 | Hủy đơn | Hủy đơn chưa thanh toán |
| 7 | Quản lý profile | Sửa tên, email |
| 8 | Sổ địa chỉ | Thêm/sửa/xóa địa chỉ |
| 9 | Xem điểm tích lũy | Điểm, hạng thành viên |
| 10 | Áp voucher | Áp mã giảm giá khi checkout |

#### 🟠 CASHIER — Thu ngân
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Đăng nhập | — |
| 2 | Mở ca | Nhập tiền đầu ca |
| 3 | Đóng ca | Chốt tiền mặt, tính chênh lệch |
| 4 | Tạo đơn POS | Tạo đơn trực tiếp tại quầy |
| 5 | Thanh toán | Xác nhận thanh toán tiền mặt |
| 6 | Quản lý đơn | Xem, cập nhật trạng thái |
| 7 | Đổi trạng thái bàn | TRONG ↔ DANG_CO_KHACH |

#### 🟣 BARISTA — Pha chế
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Đăng nhập | — |
| 2 | Xem hàng đợi | Đơn cần pha chế |
| 3 | Cập nhật trạng thái | DA_THANH_TOAN → DANG_PHA_CHE → SAN_SANG |

#### 🟡 MANAGER — Quản lý
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Dashboard | Xem tổng quan doanh thu |
| 2 | CRUD danh mục | Loại sản phẩm |
| 3 | CRUD sản phẩm | Món đồ uống |
| 4 | CRUD biến thể | Size S/M/L, giá, tồn kho |
| 5 | CRUD topping | — |
| 6 | Quản lý đơn | Xem, lọc, cập nhật |
| 7 | CRUD khuyến mãi | Chương trình, voucher |
| 8 | Quản lý khách hàng | Xem danh sách |
| 9 | CRUD nhà cung cấp | — |
| 10 | Nhập kho | Tạo, duyệt phiếu nhập |
| 11 | Quản lý ca | Xem lịch sử ca |
| 12 | CRUD bàn | Thêm, sửa, sinh QR |
| 13 | Xem báo cáo | Doanh thu, top sản phẩm |

#### 🔴 ADMIN — Quản trị viên
| STT | Chức năng | Mô tả |
|:---:|:---|:---|
| 1 | Tất cả chức năng của Manager | — |
| 2 | CRUD nhân viên | Tạo, sửa, xóa tài khoản |
| 3 | Gán vai trò | ADMIN/MANAGER/CASHIER/BARISTA |
| 4 | Vô hiệu hóa tài khoản | Bật/tắt `dang_hoat_dong` |
| 5 | Cài đặt hệ thống | — |

---

## 3. LUỒNG HOẠT ĐỘNG CHI TIẾT

Mỗi luồng được mô tả theo format: **Actor → Hành động → Hệ thống xử lý → Kết quả**

---

### 3.1. LUỒNG ĐĂNG NHẬP

**Actor:** Tất cả users

```
[Bắt đầu]
    │
    ▼
[Người dùng nhập username + password]
    │
    ▼
[Hệ thống validate input] ────► [Lỗi: 400] ────► [Hiển thị "Nhập đầy đủ"]
    │
    ▼
[Tìm user theo username trong DB]
    │
    ▼
[Kiểm tra tài khoản hoạt động] ─► [Không] ────► [Lỗi: 403] ────► [Thông báo "Tài khoản bị khóa"]
    │
    ▼
[So sánh password với hash BCrypt]
    │
    ▼
[Password đúng?] ────► [Không] ────► [Lỗi: 401] ────► [Thông báo "Sai thông tin"]
    │
    ▼
[Sinh accessToken (15p) + refreshToken (7d)]
    │
    ▼
[Lưu refreshToken vào Redis]
    │
    ▼
[Trả về JWT + thông tin user]
    │
    ▼
[Chuyển hướng theo vai trò]
    │
    ▼
[Kết thúc]
```

**Chi tiết xử lý:**
1. Frontend gửi `POST /api/auth/login` với `{username, password}`.
2. Backend validate input rỗng → 400 nếu lỗi.
3. Query bảng `nguoi_dung` theo `ten_dang_nhap`.
4. Kiểm tra `dang_hoat_dong = true`.
5. So sánh password hash BCrypt.
6. Sinh JWT access + refresh token.
7. Lưu refresh token Redis key `auth:refresh:{userId}`.
8. Trả về `{accessToken, refreshToken, user}`.

---

### 3.2. LUỒNG ĐĂNG KÝ TÀI KHOẢN

**Actor:** GUEST

```
[Bắt đầu]
    │
    ▼
[Guest mở /register]
    │
    ▼
[Nhập SĐT + họ tên + password + email]
    │
    ▼
[Validate SĐT format] ─────► [Sai] ──► [Lỗi: 400]
    │
    ▼
[Kiểm tra SĐT tồn tại] ────► [Có] ──► [Lỗi: 400] ──► ["SĐT đã được đăng ký"]
    │
    ▼
[Hash password bằng BCrypt]
    │
    ▼
[Tạo bản ghi khach_hang với:]
[  diem_tich_luy=0, hang_thanh_vien='DONG', da_xac_thuc=false]
    │
    ▼
[Trả về 201 Created]
    │
    ▼
[Chuyển đến /login]
    │
    ▼
[Kết thúc]
```

---

### 3.3. LUỒNG XEM THỰC ĐƠN

**Actor:** GUEST, CUSTOMER

```
[Bắt đầu]
    │
    ▼
[Người dùng mở /menu]
    │
    ▼
[Frontend gọi GET /api/categories] ────► [Backend query loai_san_pham]
    │
    ▼
[Frontend gọi GET /api/products] ──────► [Backend query san_pham + bien_the]
    │
    ▼
[Hiển thị grid sản phẩm]
    │
    ▼
[Người dùng chọn danh mục]
    │
    ▼
[Gọi GET /api/products?categoryId=X]
    │
    ▼
[Cập nhật grid]
    │
    ▼
[Người dùng bấm vào sản phẩm]
    │
    ▼
[Chuyển đến /menu/{id}]
    │
    ▼
[Gọi GET /api/products/{id}/variants + /api/toppings]
    │
    ▼
[Hiển thị chi tiết + biến thể + topping]
    │
    ▼
[Kết thúc]
```

---

### 3.4. LUỒNG THÊM MÓN VÀO GIỎ

**Actor:** GUEST, CUSTOMER

```
[Bắt đầu]
    │
    ▼
[Chọn size + topping + đường + đá]
    │
    ▼
[Bấm "Thêm vào giỏ"]
    │
    ▼
[Frontend POST /api/carts/items]
    │
    ▼
[Backend kiểm tra tồn kho]
    │
    ▼
[Tồn kho đủ?] ────► [Không] ──► [Lỗi: 400] ──► ["Hết hàng"]
    │
    ▼
[Là Guest?]
    │
    ├──► [Có] ──► [Sinh X-Cart-Token UUID]
    │              │
    │              ▼
    │           [Tạo gio_hang mới]
    │
    └──► [Không] ──► [Dùng ma_khach_hang từ JWT]
                     │
                     ▼
                  [Tìm/tạo gio_hang của customer]
    │
    ▼
[Tạo chi_tiet_gio_hang + topping_gio_hang]
    │
    ▼
[Tính thanh_tien = (giá + topping) × số lượng]
    │
    ▼
[Trả về giỏ hàng + header X-Cart-Token (nếu guest)]
    │
    ▼
[Frontend lưu X-Cart-Token vào localStorage]
    │
    ▼
[Toast "Đã thêm vào giỏ"]
    │
    ▼
[Kết thúc]
```

---

### 3.5. LUỒNG MERGE GIỎ GUEST → CUSTOMER

**Actor:** CUSTOMER

```
[Bắt đầu: Guest đã có giỏ với X-Cart-Token=abc-123]
    │
    ▼
[Guest đăng nhập thành công]
    │
    ▼
[Frontend gọi POST /api/carts/merge]
[  Header: X-Cart-Token: abc-123]
[  Header: Authorization: Bearer <JWT>]
    │
    ▼
[Backend tìm giỏ guest theo token]
    │
    ▼
[Backend tìm giỏ customer theo ma_khach_hang]
    │
    ▼
[Customer đã có giỏ?]
    │
    ├──► [Không] ──► [Chuyển giỏ guest thành giỏ customer]
    │
    └──► [Có] ──► [Merge items:]
                   [  - Nếu cùng cấu hình: cộng dồn số lượng]
                   [  - Nếu khác: thêm món mới]
    │
    ▼
[Xóa giỏ guest]
    │
    ▼
[Trả về giỏ customer sau merge]
    │
    ▼
[Frontend xóa X-Cart-Token khỏi localStorage]
    │
    ▼
[Redirect /cart]
    │
    ▼
[Kết thúc]
```

---

### 3.6. LUỒNG ĐẶT HÀNG ONLINE

**Actor:** GUEST, CUSTOMER

```
[Bắt đầu: Giỏ có ít nhất 1 món]
    │
    ▼
[Bấm "Tiến hành thanh toán"]
    │
    ▼
[Hiển thị checkout stepper 3 bước]
    │
    ▼
[Bước 1: Thông tin giao hàng]
[  Guest: nhập SĐT + tên + địa chỉ]
[  Customer: chọn từ sổ địa chỉ]
    │
    ▼
[Bước 2: Chọn phương thức thanh toán]
    │
    ▼
[Bước 3: Xác nhận]
    │
    ▼
[Frontend POST /api/orders]
[  Body: {loaiPhucVu, kenhDatHang, maBan?, maVoucher?, ghiChu?}]
    │
    ▼
[Backend validate giỏ không rỗng]
    │
    ▼
[Là Guest?] ──► [Có] ──► [Tạo/tìm khach_hang theo SĐT]
    │
    ▼
[Sinh mã đơn DH-{YYYY}-{seq}]
[  Dùng counter DB atomic]
    │
    ▼
[Snapshot giá: copy gia_ban vào don_gia]
    │
    ▼
[Áp voucher (nếu có) → so_tien_giam]
    │
    ▼
[Tính tong_tien_thanh_toan = tong_tien_hang - so_tien_giam]
    │
    ▼
[Tạo don_hang với trang_thai=CHO_XAC_NHAN]
    │
    ▼
[Tạo chi_tiet_don_hang + topping_don_hang]
    │
    ▼
[Xóa giỏ hàng]
    │
    ▼
[Nếu loaiPhucVu=TAI_CHO → cập nhật bàn DANG_CO_KHACH]
    │
    ▼
[Trả về mã đơn DH-2026-XXX]
    │
    ▼
[Chuyển đến /checkout/payment]
    │
    ▼
[Kết thúc]
```

---

### 3.7. LUỒNG THANH TOÁN VNPAY

**Actor:** CUSTOMER

```
[Bắt đầu: Đơn hàng đã tạo]
    │
    ▼
[Chọn phương thức VNPay]
    │
    ▼
[Frontend POST /api/payments]
[  Body: {maDonHang, phuongThucThanhToan: VNPAY, soTien}]
    │
    ▼
[Backend tạo giao_dich_thanh_toan]
[  trang_thai = CHO_XU_LY]
    │
    ▼
[Backend sinh paymentUrl với HMAC-SHA512]
    │
    ▼
[Trả về paymentUrl cho frontend]
    │
    ▼
[Frontend redirect đến VNPay]
    │
    ▼
[Khách thanh toán trên VNPay]
    │
    ▼
[VNPay gửi IPN callback đến backend]
[  POST /api/payments/callback/vnpay]
    │
    ▼
[Backend verify HMAC signature]
    │
    ▼
[Signature hợp lệ?] ────► [Không] ──► [Từ chối, log warning]
    │
    ▼
[Check idempotency: vnp_TxnRef đã xử lý chưa?]
    │
    ▼
[Đã xử lý?] ────► [Có] ──► [Bỏ qua, ack VNPay]
    │
    ▼
[vnp_ResponseCode == "00"?]
    │
    ├──► [Không] ──► [Update giao_dich = THAT_BAI]
    │
    └──► [Có] ──► [Update giao_dich = THANH_CONG]
                  │
                  ▼
              [Update don_hang = DA_THANH_TOAN]
                  │
                  ▼
              [Trừ tồn kho biến thể + topping]
                  │
                  ▼
              [Ack VNPay]
    │
    ▼
[VNPay redirect khách về returnUrl]
[  /checkout/payment/callback?orderId=X]
    │
    ▼
[Frontend gọi GET /api/payments/verify/{orderId}]
    │
    ▼
[Backend trả về trạng thái thực tế]
    │
    ▼
[Payment THANH_CONG?]
    │
    ├──► [Có] ──► [Redirect /order-success]
    │
    └──► [Không] ──► [Redirect /checkout/payment?retry]
    │
    ▼
[Kết thúc]
```

---

### 3.8. LUỒNG QUẢN LÝ ĐƠN HÀNG (ADMIN)

**Actor:** CASHIER, MANAGER

```
[Bắt đầu]
    │
    ▼
[Vào /admin/orders]
    │
    ▼
[Frontend GET /api/orders?page=0&size=20]
    │
    ▼
[Hiển thị bảng đơn với filter]
    │
    ▼
[Chọn 1 đơn]
    │
    ▼
[Gọi GET /api/orders/{id}]
    │
    ▼
[Hiển thị chi tiết đơn + timeline]
    │
    ▼
[Chọn trạng thái mới]
    │
    ▼
[Frontend PATCH /api/orders/{id}/status]
[  Body: {trangThaiMoi, lyDo?}]
    │
    ▼
[Backend validate state machine]
    │
    ▼
[Transition hợp lệ?] ────► [Không] ──► [Lỗi: 400]
    │
    ▼
[Trạng thái mới = DA_THANH_TOAN?]
    │
    ├──► [Có] ──► [Trừ tồn kho (biến thể + topping)]
    │
    └──► [Không]
    │
    ▼
[Trạng thái mới = DA_HUY?]
    │
    ├──► [Có] ──► [Hoàn tồn kho + rollback voucher]
    │
    └──► [Không]
    │
    ▼
[Update don_hang.trang_thai]
    │
    ▼
[Trả về đơn sau update]
    │
    ▼
[Kết thúc]
```

**State machine:**
```
CHO_XAC_NHAN → DA_THANH_TOAN → DANG_PHA_CHE → SAN_SANG → HOAN_THANH
                   │
                   └──────► DA_HUY (bất kỳ, trừ HOAN_THANH)
```

---

### 3.9. LUỒNG MỞ/ĐÓNG CA

**Actor:** CASHIER

```
=== MỞ CA ===
[Bắt đầu]
    │
    ▼
[Cashier vào POS]
    │
    ▼
[Frontend kiểm tra GET /api/shifts/current]
    │
    ▼
[Đã có ca DANG_MO?] ────► [Có] ──► [Thông báo đã có ca]
    │
    ▼
[Nhập tienDauCa]
    │
    ▼
[POST /api/shifts/open]
    │
    ▼
[Tạo ca_lam_viec với trang_thai=DANG_MO]
    │
    ▼
[Trả về ca vừa tạo]
    │
    ▼
[Sẵn sàng tạo đơn]

=== ĐÓNG CA ===
[Bắt đầu]
    │
    ▼
[Bấm "Đóng ca"]
    │
    ▼
[Backend tính doanhThuTienMat trong ca]
[  Sum đơn hàng TIEN_MAT với trang_thai=HOAN_THANH]
    │
    ▼
[Cashier nhập tienKetCa]
    │
    ▼
[Tính chenhLech = tienKetCa - (tienDauCa + doanhThu)]
    │
    ▼
[Update ca: trang_thai=DA_DONG, thoi_gian_ket_thuc=now()]
    │
    ▼
[Trả về báo cáo ca]
    │
    ▼
[Kết thúc]
```

---

### 3.10. LUỒNG PHA CHẾ (BARISTA)

**Actor:** BARISTA

```
[Bắt đầu]
    │
    ▼
[Barista mở màn hình KDS]
    │
    ▼
[Frontend GET /api/orders?status=DA_THANH_TOAN]
    │
    ▼
[Hiển thị danh sách đơn cần pha chế]
    │
    ▼
[Chọn 1 đơn]
    │
    ▼
[PATCH /api/orders/{id}/status]
[  Body: {trangThaiMoi: DANG_PHA_CHE}]
    │
    ▼
[Backend validate + update]
    │
    ▼
[Pha chế xong]
    │
    ▼
[PATCH /api/orders/{id}/status]
[  Body: {trangThaiMoi: SAN_SANG}]
    │
    ▼
[Giao khách]
    │
    ▼
[PATCH /api/orders/{id}/status]
[  Body: {trangThaiMoi: HOAN_THANH}]
    │
    ▼
[Kết thúc]
```

---

### 3.11. LUỒNG NHẬP KHO

**Actor:** MANAGER

```
[Bắt đầu]
    │
    ▼
[Vào /admin/inventory]
    │
    ▼
[Bấm "Tạo phiếu nhập"]
    │
    ▼
[Chọn nhà cung cấp]
    │
    ▼
[Thêm mặt hàng:]
[  - Biến thể sản phẩm (bánh, lon)]
[  - Hoặc topping (trân châu, thạch)]
    │
    ▼
[Nhập số lượng + đơn giá]
    │
    ▼
[POST /api/purchase-orders]
    │
    ▼
[Backend sinh mã phiếu PN-{YYYY}-{seq}]
    │
    ▼
[Tạo phieu_nhap_hang với trang_thai=CHO_DUYET]
    │
    ▼
[Tạo chi_tiet_phieu_nhap]
    │
    ▼
[Manager duyệt phiếu]
    │
    ▼
[PATCH /api/purchase-orders/{id}/approve]
    │
    ▼
[Backend cộng so_luong_ton cho biến thể/topping]
    │
    ▼
[Update trang_thai=DA_NHAP_KHO]
    │
    ▼
[Idempotency: không cho duyệt 2 lần]
    │
    ▼
[Kết thúc]
```

---

### 3.12. LUỒNG XEM BÁO CÁO DOANH THU

**Actor:** MANAGER

```
[Bắt đầu]
    │
    ▼
[Vào /admin/reports]
    │
    ▼
[Chọn khoảng thời gian]
    │
    ▼
[Chọn groupBy: DAY/WEEK/MONTH]
    │
    ▼
[Frontend GET /api/reports/revenue?from&to&groupBy]
    │
    ▼
[Backend native query GROUP BY DATE_TRUNC]
[  Sum tong_tien_thanh_toan]
[  Filter trang_thai=HOAN_THANH]
    │
    ▼
[Cache Redis TTL 5 phút]
    │
    ▼
[Trả về List<DoanhThuTheoNgay>]
    │
    ▼
[Hiển thị biểu đồ Recharts]
    │
    ▼
[Kết thúc]
```

---

### 3.13. LUỒNG TÍCH ĐIỂM KHÁCH HÀNG

**Actor:** CUSTOMER

```
[Bắt đầu: Đơn hàng chuyển sang HOAN_THANH]
    │
    ▼
[Backend lắng nghe event hoặc hook trong OrderService]
    │
    ▼
[Đơn hàng có ma_khach_hang?]
    │
    ├──► [Không] ──► [Bỏ qua]
    │
    └──► [Có] ──► [Tính điểm = tong_tien_thanh_toan / 10000]
                  │
                  ▼
              [Cộng vào khach_hang.diem_tich_luy]
                  │
                  ▼
              [Kiểm tra ngưỡng hạng:]
              [  ĐỒNG: 0-99]
              [  BẠC: 100-499]
              [  VÀNG: 500-999]
              [  KIM_CƯƠNG: 1000+]
                  │
                  ▼
              [Cập nhật hang_thanh_vien nếu đủ điều kiện]
                  │
                  ▼
              [Log thay đổi điểm]
    │
    ▼
[Kết thúc]
```

---

### 3.14. LUỒNG TRA CỨU ĐƠN (GUEST)

**Actor:** GUEST

```
[Bắt đầu]
    │
    ▼
[Guest mở /track-order]
    │
    ▼
[Nhập mã đơn + SĐT]
    │
    ▼
[Frontend GET /api/orders/track?maDon&soDienThoai]
    │
    ▼
[Backend query don_hang theo ma_hien_thi_don]
    │
    ▼
[Tìm thấy đơn?] ────► [Không] ──► [Lỗi: 404]
    │
    ▼
[So khớp SĐT với khach_hang liên kết]
    │
    ▼
[SĐT khớp?] ────► [Không] ──► [Lỗi: 404 (không tiết lộ)]
    │
    ▼
[Trả về chi tiết đơn + trạng thái]
    │
    ▼
[Hiển thị timeline trạng thái]
    │
    ▼
[Kết thúc]
```

---

## 4. TỔNG KẾT

### 4.1. Thống kê

| Hạng mục | Số lượng |
|:---|:---:|
| Tổng số module | 12 |
| Tổng số chức năng | 60+ |
| Tổng số actor | 6 |
| Tổng số luồng chính | 14 |
| Tổng số endpoints | 60+ |
| Tổng số bảng database | 25 |

### 4.2. Trạng thái triển khai

| Module | Backend | Frontend |
|:---|:---:|:---:|
| auth | ✅ | ✅ |
| catalog | ✅ | ✅ |
| cart | ✅ | ✅ |
| order | ✅ | ✅ |
| user | ✅ | ⚠️ |
| payment | 🟡 | 🟡 |
| promotion | 🟡 | 🟡 |
| shift | 🟡 | 🟡 |
| table | 🟡 | 🟡 |
| customer | 🟡 | 🟡 |
| inventory | 🟡 | 🟡 |
| report | 🟡 | 🟡 |

✅ Hoàn thành | 🟡 Đang phát triển | ⚠️ Cần bổ sung

### 4.3. Business Rules quan trọng

| Rule | Mô tả |
|:---|:---|
| Snapshot giá | Copy giá vào đơn khi tạo, không đổi sau |
| State machine | Chỉ chuyển trạng thái hợp lệ |
| Optimistic locking | Dùng `@Version` cho inventory |
| Sinh mã đơn | Counter DB atomic, format DH-YYYY-NNN |
| Tích điểm | 1 điểm / 10,000 VNĐ |
| Thứ tự KM | Sản phẩm → Hóa đơn → Voucher |
| Guest cart | Dùng `X-Cart-Token` trong localStorage |
| Payment idempotency | Check `transaction_ref` trước xử lý callback |

---

**HẾT TÀI LIỆU**
