# 📚 TỔNG QUAN CÁC MODULE — Dự án MilkTea

> Đối chiếu controller/service/entity, Flyway V1–V5 và [03_enums_reference.md](03_enums_reference.md). “Route có” chỉ có nghĩa controller khai báo URL; các scaffold còn trả null/TODO thì chưa chạy nghiệp vụ end-to-end. Quy tắc không có trong code được đánh dấu cần Product Owner (PO) xác nhận.

## Phần 1: Bản đồ tổng thể

```text
                          ┌────────────────┐
                          │ ✅ Auth + User │
                          └───────┬────────┘
                                  │ xác thực / role
                                  v
┌────────────┐ chọn món ┌───────────┐ checkout ┌───────────┐ thu tiền ┌────────────┐
│✅ Catalog  │─────────>│ ✅ Cart   │────────>│ ✅ Order  │────────>│🟡 Payment  │
└─────┬──────┘           └────┬──────┘         └──┬──┬──┬──┘         └────────────┘
      ├── promotion/voucher ─────────────────────>│  │  └──report──>🟡 Report
      └── hàng nhập <── 🟡 Inventory              │  └──shift─────>🟡 Shift
                              │                   └────table/QR──>🟡 Table
                              └──────── customer / loyalty ──────>🟡 Customer

✅ Hoàn thành luồng chính: auth, user (mô hình), catalog, cart, order.
🟡 Cần partner hoàn thiện: payment, promotion, shift, table, customer, inventory, report.
```

## Phần 2: Chi tiết từng module

### 🟢 Auth — Trạng thái: Hoàn thành

**1. Bài toán:** Cho nhân viên/khách đăng nhập và cấp phiên truy cập. Tài khoản bị vô hiệu hóa không đăng nhập được; refresh token có thể thu hồi.
**2. Entity/Table:** Dùng `NguoiDung`/`nguoi_dung` và `VaiTro`/`vai_tro` của User.
**3. API chính:**

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`, `/api/auth/logout` | Đăng nhập, đăng ký CUSTOMER, gia hạn, đăng xuất |
| GET | `/api/auth/me` | Xem tài khoản hiện tại |

**4. Rules:** username unique; login sai hoặc inactive đều bị từ chối; đăng ký gán CUSTOMER; refresh token được đánh dấu trên Redis và có TTL.
**5. Thứ tự:** login → refresh/logout → kiểm tra role trên API.
**6. Phụ thuộc:** User, Redis/security; các API bảo vệ phụ thuộc token.
**Bắt đầu từ đâu:** `AuthController`, `AuthServiceImpl`, `SecurityConfig`.

### 🟢 User — Trạng thái: Hoàn thành phần mô hình

**1. Bài toán:** Lưu tài khoản nhân viên/quản trị cùng vai trò để kiểm soát thao tác. Khách hàng thành viên được lưu ở module Customer riêng.
**2. Entity/Table:** `NguoiDung`/`nguoi_dung` lưu tài khoản; `VaiTro`/`vai_tro` lưu vai trò.
**3. API chính:** Không có UserController; đăng ký qua POST `/api/auth/register`, profile qua GET `/api/auth/me`.
**4. Rules:** role seed gồm ADMIN, MANAGER, CASHIER, BARISTA, CUSTOMER; username unique; tài khoản inactive không login; register gán CUSTOMER.
**5. Thứ tự:** seed role → auth → API quản lý nhân viên nếu cần.
**6. Phụ thuộc:** Auth và API cần phân quyền.
**Bắt đầu từ đâu:** `user/entity`, `NguoiDungRepository`, migration V1/V2.

### 🟢 Catalog — Trạng thái: Hoàn thành

**1. Bài toán:** Quản lý danh mục, món, size/giá và topping để POS/khách chọn món. Quản lý cập nhật tình trạng bán và tồn.
**2. Entity/Table:** `LoaiSanPham`/`loai_san_pham` nhóm món; `SanPham`/`san_pham`; `BienTheSanPham`/`bien_the_san_pham` size/giá/tồn; `Topping`/`topping`.
**3. API chính:**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/categories`, `/api/products` | Xem/tạo nhóm và món |
| GET/POST | `/api/products/{id}/variants` | Xem/thêm size |
| GET/POST | `/api/toppings` | Xem/tạo topping |
| PATCH | `/api/variants/{id}/stock` | Điều chỉnh tồn size |

Có thêm GET/PUT/DELETE cho category/product/topping và PUT variant theo controller.
**4. Rules:** size unique trong cùng món; không xóa danh mục còn sản phẩm; ví dụ giá 50,000₫ cần lưu snapshot trên dòng đơn nếu giá catalog đổi sau đó.
**5. Thứ tự:** CRUD danh mục/món → size/topping → tích hợp cart/order.
**6. Phụ thuộc:** Cart, Order, Promotion, Inventory.
**Bắt đầu từ đâu:** `catalog/service/impl` và controller tương ứng.

### 🟢 Cart — Trạng thái: Hoàn thành

**1. Bài toán:** Giữ món khách chọn trước khi đặt, kể cả chưa đăng nhập. Cho chỉnh số lượng và tùy chọn đường/đá/topping.
**2. Entity/Table:** `GioHang`/`gio_hang` (khách hoặc token phiên); `ChiTietGioHang`/`chi_tiet_gio_hang`; `ToppingGioHang`/`topping_gio_hang`.
**3. API chính:**

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/carts/items` | Thêm món |
| GET | `/api/carts/current` | Xem giỏ hiện tại |
| PATCH/DELETE | `/api/carts/items/{id}` | Sửa/xóa dòng |
| DELETE | `/api/carts/current` | Xóa giỏ |

**4. Rules:** quantity > 0; giá tính ở server; guest cart nhận diện bằng token phiên; món/topping phải tồn tại và được bán.
**5. Thứ tự:** nhận diện giỏ → thêm/sửa/xóa → chuyển thành Order.
**6. Phụ thuộc:** Catalog, Customer, Order.
**Bắt đầu từ đâu:** `CartController`, `CartServiceImpl`.

### 🟢 Order — Trạng thái: Hoàn thành luồng chính

**1. Bài toán:** Gom món thành đơn có mã, tổng tiền, kênh và trạng thái từ lúc nhận tới hoàn thành. Dùng cho POS và các kênh online.
**2. Entity/Table:** `DonHang`/`don_hang`; `ChiTietDonHang`/`chi_tiet_don_hang`; `ToppingDonHang`/`topping_don_hang`.
**3. API chính:**

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/orders`, `/api/orders/pos` | Tạo từ giỏ / tạo tại POS |
| GET | `/api/orders`, `/api/orders/my`, `/api/orders/{id}` | Danh sách / đơn của tôi / chi tiết |
| PATCH/POST | `/api/orders/{id}/status`, `/api/orders/{id}/cancel` | Đổi trạng thái / hủy |

**4. Rules:** trạng thái theo enum reference; quantity > 0; mã đơn dùng sequence theo năm; tính tổng ở server; V5 thêm cờ trừ kho và version chống cập nhật đồng thời.
**5. Thứ tự:** tạo đơn/snapshot giá → status/hủy → payment, shift, table, promotion.
**6. Phụ thuộc:** Cart/Catalog; tích hợp tiếp Payment, Shift, Table, Customer, Promotion, Report.
**Bắt đầu từ đâu:** `OrderServiceImpl`; đọc transaction và logic tồn kho.

### 🟡 Payment — Trạng thái: Cần implement

**1. Bài toán:** Ghi nhận tiền khách trả cho đơn qua tiền mặt hoặc cổng điện tử. Đơn chỉ được xác nhận đã trả sau khi có kết quả hợp lệ.
**2. Entity/Table:** `GiaoDichThanhToan`/`giao_dich_thanh_toan` lưu đơn, tiền, phương thức và trạng thái.
**3. API chính (scaffold, một số handler trả null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/payments` | Tra/khởi tạo giao dịch |
| GET/PUT/DELETE | `/api/payments/{id}` | Tra/cập nhật/xóa theo controller hiện tại |

**4. Rules:** phương thức TIEN_MAT/CHUYEN_KHOAN_QR/MOMO/VNPAY/THE_NGAN_HANG; trạng thái CHO_XU_LY/THANH_CONG/THAT_BAI/HOAN_TIEN; ví dụ order 100,000₫ chỉ ghi trả đủ khi giao dịch hợp lệ đạt số cần thu; callback lặp không xử lý hai lần. Quy tắc thiếu/thừa tiền cần PO.
**5. Thứ tự:** tiền mặt → transaction/idempotency → gateway/callback → refund.
**6. Phụ thuộc:** Order; gateway ngoài; Report dùng dữ liệu thanh toán.
**Bắt đầu từ đâu:** `GiaoDichThanhToanServiceImpl`, `PaymentGatewayFactory`; adapter online còn TODO.

### 🟡 Promotion — Trạng thái: Cần implement

**1. Bài toán:** Tạo ưu đãi theo món, tổng hóa đơn hoặc voucher để khách được giảm tiền. Chương trình có thời gian hiệu lực và cờ bật/tắt.
**2. Entity/Table:** `ChuongTrinhKhuyenMai`/`chuong_trinh_khuyen_mai`; `KhuyenMaiSanPham`/`khuyen_mai_san_pham`; `KhuyenMaiHoaDon`/`khuyen_mai_hoa_don`; `KhuyenMaiVoucher`/`khuyen_mai_voucher`.
**3. API chính (route khai báo, service TODO):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/promotion-programs` | Xem/tạo chương trình |
| GET/POST | `/api/product-promotions`, `/api/invoice-promotions` | Xem/tạo rule |
| GET/POST | `/api/vouchers` | Xem/tạo voucher |

Mỗi nhóm cũng khai báo GET/PUT/DELETE `/{id}`.
**4. Rules:** loại giảm PHAN_TRAM/TIEN_CO_DINH; voucher có ngưỡng và quota; ví dụ 10% × 100,000₫ = 10,000₫ trước cap; cộng dồn/làm tròn/thời điểm tiêu lượt chưa được quy định, cần PO.
**5. Thứ tự:** CRUD campaign/rule → hàm tính → kiểm tra quota nguyên tử → tích hợp Order.
**6. Phụ thuộc:** Catalog, Order, Customer nếu giới hạn theo khách.
**Bắt đầu từ đâu:** các `promotion/service/impl`; toàn bộ implementation có TODO.

### 🟡 Shift — Trạng thái: Cần implement

**1. Bài toán:** Thu ngân mở ca với tiền đầu ca, rồi đóng ca để so tiền thực đếm với tiền bán. Đơn POS có thể gắn ca.
**2. Entity/Table:** `CaLamViec`/`ca_lam_viec` lưu thu ngân, giờ, tiền đầu/cuối ca, trạng thái.
**3. API chính (controller hiện trả null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/shifts` | Xem/mở ca |
| GET/PUT/DELETE | `/api/shifts/{id}` | Xem/sửa/xóa scaffold |

**4. Rules:** DANG_MO → DA_DONG; chỉ ca mở mới đóng; ví dụ kỳ vọng 200,000₫ đầu ca + 350,000₫ thu tiền mặt = 550,000₫, cách tính hoàn/hủy phải chốt; không âm thầm sửa ca đã đóng.
**5. Thứ tự:** mở/đọc → đóng/đối soát → gắn Order/Payment.
**6. Phụ thuộc:** Auth/User, Order, Payment, Report.
**Bắt đầu từ đâu:** `CaLamViecServiceImpl`; schema chưa có store/chi nhánh.

### 🟡 Table — Trạng thái: Cần implement

**1. Bài toán:** Quản lý bàn trống/đang phục vụ và QR trên bàn. Đơn dùng tại quán gắn đúng bàn.
**2. Entity/Table:** `Ban`/`ban`; `DonHang`/`don_hang` có FK bàn.
**3. API chính (controller hiện trả null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/tables` | Xem/tạo bàn |
| GET/PUT/DELETE | `/api/tables/{id}` | Xem/sửa/xóa bàn |

**4. Rules:** trạng thái TRONG/DANG_CO_KHACH/DA_DAT_TRUOC; số bàn và QR token unique; token bàn 05 chỉ ánh xạ tới bàn 05; vòng đời chuyển trạng thái theo Order cần PO chốt.
**5. Thứ tự:** CRUD → QR lookup an toàn → đồng bộ Order/hủy/hoàn thành.
**6. Phụ thuộc:** Order, Catalog, Cart, Auth.
**Bắt đầu từ đâu:** `BanServiceImpl`; TODO, chưa có bảng reservation.

### 🟡 Customer — Trạng thái: Cần implement

**1. Bài toán:** Lưu hồ sơ mua hàng để tìm đơn và chăm sóc thành viên. Tài khoản khách tách với nhân viên trong User.
**2. Entity/Table:** `KhachHang`/`khach_hang` lưu điện thoại, email/tên, mật khẩu tùy chọn, điểm/hạng, xác thực.
**3. API chính (controller hiện trả null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/customers` | Tìm/tạo hồ sơ |
| GET/PUT/DELETE | `/api/customers/{id}` | Xem/sửa/xóa hồ sơ |

**4. Rules:** điện thoại unique; xác thực mặc định false; hạng DONG/BAC/VANG/KIM_CUONG; ví dụ 100,000₫ trả thành công có thể dùng làm căn cứ cộng điểm, nhưng tỷ lệ điểm và ngưỡng hạng chưa được code/enum định nghĩa—PO phải quyết định.
**5. Thứ tự:** CRUD → xác thực → ledger điểm chống cộng trùng → tích hợp Order.
**6. Phụ thuộc:** Auth, Cart, Order, Promotion.
**Bắt đầu từ đâu:** `KhachHangServiceImpl`; TODO toàn bộ.

### 🟡 Inventory — Trạng thái: Cần implement

**1. Bài toán:** Lưu nhà cung cấp, hàng mua vào và tăng tồn khi phiếu nhập được duyệt. Lịch sử phiếu giúp đối chiếu lượng/giá nhập.
**2. Entity/Table:** `NhaCungCap`/`nha_cung_cap`; `PhieuNhapHang`/`phieu_nhap_hang`; `ChiTietPhieuNhap`/`chi_tiet_phieu_nhap`.
**3. API chính (route có, service còn TODO/return null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET/POST | `/api/suppliers` | Xem/tạo nhà cung cấp |
| GET/POST | `/api/purchase-orders` | Xem/tạo phiếu nhập |
| PATCH | `/api/purchase-orders/{id}/approve` | Duyệt và cộng tồn |
| GET/POST | `/api/purchase-order-lines` | Xem/thêm dòng hàng |

Controller cũng khai báo GET/PUT/DELETE `/{id}` cho các nhóm.
**4. Rules:** CHO_DUYET → DA_NHAP_KHO hoặc DA_HUY; chỉ sửa/hủy khi chờ; mỗi dòng chọn variant hoặc topping; ví dụ tồn 12 + nhập 5 = 17, chỉ cộng đúng một lần. V1 default DA_NHAP_KHO nên service phải set CHO_DUYET tường minh.
**5. Thứ tự:** supplier CRUD → phiếu/dòng, tính tổng server → approve trong transaction/version.
**6. Phụ thuộc:** Catalog, Auth/User, Report.
**Bắt đầu từ đâu:** `PhieuNhapHangServiceImpl`; không sửa migration đã chạy.

### 🟡 Report — Trạng thái: Cần implement

**1. Bài toán:** Giúp quản lý xem tổng hợp bán hàng theo thời gian, thay vì cộng đơn thủ công. Báo cáo đọc dữ liệu nghiệp vụ, không tạo giao dịch.
**2. Entity/Table:** Không có entity riêng; đọc `DonHang`/`don_hang`, `GiaoDichThanhToan`/`giao_dich_thanh_toan`, `CaLamViec`/`ca_lam_viec`.
**3. API chính (controller hiện trả null):**

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/reports` | Summary; query filter chưa định nghĩa |

**4. Rules:** chỉ cộng trạng thái hợp lệ; ví dụ 50,000₫ + 80,000₫ = 130,000₫ trước refund; timezone/biên ngày nhất quán; định nghĩa doanh thu và quyền xem cần PO.
**5. Thứ tự:** chốt KPI → query theo ngày/trạng thái → breakdown theo ca/kênh.
**6. Phụ thuộc:** Order, Payment, Shift; Inventory cho báo cáo nhập.
**Bắt đầu từ đâu:** `ReportServiceImpl` và PO chốt chỉ số.

## Phần 3: Thứ tự implement đề xuất

Ước tính là ngày công code + rà soát cho một partner quen Spring/JPA; chưa gồm thời gian chờ PO hoặc sandbox. Module độc lập có thể làm song song.

| # | Module | Độ khó | Thời gian | Lý do/dependency |
|---:|---|---|---:|---|
| 1 | Shift | ⭐ | 1–2 ngày | Nhỏ, auth đã có; đối soát cần Payment, gắn đơn cần Order |
| 2 | Table | ⭐ | 1–2 ngày | CRUD/QR đơn giản; đồng bộ vòng đời cần Order |
| 3 | Customer | ⭐⭐ | 3–5 ngày | CRUD vừa sức; loyalty cần PO, Cart/Order dùng profile |
| 4 | Inventory | ⭐⭐ | 4–6 ngày | Cần Catalog; cộng tồn transaction/version |
| 5 | Promotion | ⭐⭐⭐ | 5–8 ngày | Rule/quota phức tạp, phụ thuộc Catalog/Order/Customer |
| 6 | Payment | ⭐⭐⭐ | 6–10 ngày | Order phải ổn định; callback/gateway cần sandbox |
| 7 | Report | ⭐⭐ | 3–5 ngày | Query vừa sức nhưng KPI cần Order/Payment/Shift thống nhất |

### Vì sao theo thứ tự này?

1. **Shift:** dễ làm quen scaffold; bắt đầu CRUD/trạng thái, bổ sung đối soát sau khi Payment rõ.
2. **Table:** ít dữ liệu, cần Order để cập nhật bàn theo vòng đời đơn.
3. **Customer:** schema sẵn, nhưng phải chốt OTP và điểm/hạng; tích hợp Cart/Order.
4. **Inventory:** dựa Catalog để cộng tồn variant/topping, cần đảm bảo approve chỉ cộng một lần.
5. **Promotion:** cần chốt cộng dồn/cap/quota; áp dụng lên Catalog/Order, có thể giới hạn Customer.
6. **Payment:** cần order amount/status; adapter online cần credentials và xác minh callback.
7. **Report:** có thể viết query sớm nhưng kết quả đáng tin cần semantics của payment, order và shift.

## Phần 4: Checklist trước khi bắt đầu

- [ ] Đã đọc [docs/03_enums_reference.md](03_enums_reference.md).
- [ ] Đã chạy `docker compose up -d` và xác nhận database healthy.
- [ ] Đã login admin qua Swagger UI (backend có Swagger/API, chưa có frontend POS).
- [ ] Đã đọc TODO trong ServiceImpl của module sẽ làm.
- [ ] Đã phân biệt route khai báo với logic placeholder/null.
- [ ] Đã hỏi PO về điểm/hạng, cộng dồn khuyến mãi và đối soát ca.

## Ghi chú về mức độ API

Các endpoint scaffold ở Payment/Promotion/Shift/Table/Customer/Report không cam kết hoạt động: một số handler trả `ApiResponse.success(null)`, service impl throw TODO. Đọc DTO, Swagger và implementation trước khi phụ thuộc contract. Endpoint hoàn thành được liệt kê theo mapping trong controller hiện tại.


