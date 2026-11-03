# 📘 TÀI LIỆU CẬP NHẬT BACKEND & HƯỚNG DẪN HOÀN THIỆN NGHIỆP VỤ MÁY POS (`koithe-pos`)

> **Cập nhật lần cuối:** 04/11/2026  
> **Repository Backend:** `https://github.com/KhoiHoang16/koithe-api.git` (Nhánh chuẩn: **`integration/api-baseline`**)  
> **Repository Máy POS:** `https://github.com/JohnPattle/koithe-pos.git` (Đối chiếu trực tiếp trên nhánh **`main`**)  
> **Cổng chạy Backend (Docker):** `http://localhost:8081` (Swagger UI: `http://localhost:8081/swagger-ui.html`)

---

## PHẦN 1: CÁCH KÉO CODE & KHỞI CHẠY LẠI BACKEND MỚI NHẤT

> [!IMPORTANT]
> **Lưu ý quan trọng khi chạy Docker Backend:**  
> Nếu chỉ gõ `docker compose up -d`, Docker sẽ **dùng lại image cũ** và **KHÔNG** biên dịch các thay đổi code Java mới. Bắt buộc phải thêm cờ `--build` khi cập nhật Backend!

```bash
cd koithe-api
git fetch origin
git checkout integration/api-baseline
git pull origin integration/api-baseline

# Bắt buộc có --build để Docker đóng gói lại file .jar mới nhất
docker compose up -d --build backend
```

---

## PHẦN 2: NHỮNG GÌ ĐÃ THAY ĐỔI & BỔ SUNG TRÊN BACKEND (`koithe-api`)

Để máy POS (`koithe-pos`) và Website (`koithe-fe`) hoạt động đồng bộ 100% trên cùng một cơ sở dữ liệu, Backend đã hoàn thiện và nâng cấp 5 cụm nghiệp vụ sau:

### 1. Ràng buộc Ca làm việc (`CaLamViec`) khi đặt đơn & Tự động gán đơn vào Ca đang mở
- **Vấn đề trước đây:** Khách đặt đơn từ Website hoặc quét QR bàn tạo ra bản ghi `don_hang` có `ma_ca = NULL` và `ma_thu_ngan = NULL`, khiến đơn không hiển thị trong ca hiện tại của máy POS và không cộng vào doanh thu ca. Ngoài ra khi quán chưa mở ca nào, khách vẫn đặt hàng được.
- **Cơ chế mới trên Backend (`OrderServiceImpl` & `CaLamViecServiceImpl`):**
  1. **Chặn đặt hàng khi chưa có ca mở:** Khi gọi `POST /api/orders`, Backend bắt buộc kiểm tra phải có ca làm việc đang ở trạng thái `DANG_MO`. Nếu tất cả các ca đều `DA_DONG`, API trả lỗi `400 Bad Request`:
     > `"Hiện chưa có ca làm việc nào đang mở. Vui lòng mở ca làm việc trước khi tạo đơn hàng!"`
  2. **Tự động gán `ma_ca` (`shiftId`) và `ma_thu_ngan` (`cashierId`):** Khi có ca đang mở (`DANG_MO`), mọi đơn đặt từ Website/QR sẽ tự động lấy `ma_ca` và `ma_thu_ngan` của ca đang mở đó.
  3. **Tự động gom đơn treo khi mở ca mới:** Khi thu ngân mở ca mới (`POST /api/shifts` hoặc `POST /api/shifts/open`), Backend tự động gắn các đơn chưa hủy mà `ma_ca IS NULL` (nếu có) vào ca vừa mở.

---

### 2. Đồng bộ Thanh toán Tiền mặt (`TIEN_MAT`) & Chuẩn hóa Đối soát Ca (`CaLamViecResponse`)
- **Tự động chốt thu tiền mặt khi duyệt đơn:**
  - Khi đơn hàng chuyển trạng thái sang `DA_THANH_TOAN` hoặc `HOAN_THANH` (qua `PATCH /api/orders/{id}/status`), Backend tự động cập nhật giao dịch `TIEN_MAT` đang ở `CHO_XU_LY` của đơn đó sang `THANH_CONG`.
  - Đồng thời khi đơn chuyển sang `DA_THANH_TOAN` (từ `POST /api/payments` với phương thức `TIEN_MAT` hoặc từ `POST /api/payments/dev-simulate-success` với QR/MoMo/VNPay), Backend **tự động trừ tồn kho biến thể (`BienTheSanPham`) và `Topping`** ngay trong cùng transaction.
- **Endpoint xác nhận thu tiền mặt riêng (nếu cần gọi trực tiếp):**
  - `POST /api/payments/{id}/confirm-cash`
  - `POST /api/payments/confirm-cash?orderId={orderId}`
- **Chuẩn hóa công thức Đối soát tiền mặt & bổ sung tên thu ngân trong `CaLamViecResponse`:**
  - `tienMatDuKien = tienDauCa + sumTienMatThanhCongByCaId(caId)` *(chỉ cộng các giao dịch `TIEN_MAT` có trạng thái `THANH_CONG`, không cộng nhầm đơn chuyển khoản QR/MoMo/VNPay hay đơn hủy)*.
  - `chenhLech = tienKetCa - tienMatDuKien`.
  - Response của `/api/shifts`, `/api/shifts/current`, `/api/shifts/{id}` nay trả thêm **`maThuNgan`** và **`tenThuNgan`**:
    ```json
    {
      "id": 4,
      "maThuNgan": 1,
      "tenThuNgan": "Administrator",
      "thoiGianBatDau": "2026-11-03T16:46:37.449Z",
      "thoiGianKetThuc": null,
      "tienDauCa": 1500000.00,
      "tienKetCa": 0.00,
      "trangThai": "DANG_MO",
      "tongDoanhThu": 125000.00,
      "soDonHang": 2,
      "tienMatDuKien": 1565000.00,
      "chenhLech": null
    }
    ```
- **Phân quyền đóng ca:** Chỉ chủ ca (`maThuNgan == currentUser.id`) hoặc tài khoản `ADMIN` / `MANAGER` mới được đóng ca (`POST /api/shifts/{id}/close`). Người khác đóng ca hộ sẽ nhận HTTP `403 Forbidden`.

---

### 3. Bổ sung Module Quản lý Nhân viên (`/api/users`) & Báo cáo Thống kê (`/api/reports/summary`)
Backend đã mở đầy đủ các endpoint cho Quản lý nhân viên và Báo cáo doanh thu (chi tiết DTO và cách gắn vào POS nằm ở **Phần 3** bên dưới).

### 4. Khôi phục Khách hàng xóa mềm & Bảo toàn lượt dùng Voucher
- **`POST /api/customers`**: Nếu tạo mới khách hàng có SĐT trùng với bản ghi đã xóa mềm (`deleted_at IS NOT NULL`), Backend tự động khôi phục (`restore()`) và cập nhật lại thông tin thay vì báo lỗi 500.
- **`PUT /api/vouchers/{id}`**: Khi sửa voucher đang diễn ra, Backend giữ nguyên `soLuotDaDung` hiện có trong DB, không bị reset về 0.

---

## PHẦN 3: RÀ SOÁT NHÁNH `main` CỦA `koithe-pos` & HƯỚNG DẪN HOÀN THIỆN CHI TIẾT

Qua đọc mã nguồn nhánh **`main`** hiện tại của `koithe-pos`, các phân hệ **Đăng nhập (`authApi.ts`), Bán hàng tại quầy (`orderApi.ts`), Thanh toán (`paymentApi.ts`), Ca làm việc (`shiftApi.ts`), Bàn (`tableApi.ts`), Khách hàng (`customerApi.ts`), Kho & Phiếu nhập (`inventoryApi.ts`), Khuyến mãi (`promotionApi.ts`)** đã được thiết kế và gán API rất tốt.

Để hoàn thiện nốt các nghiệp vụ còn đang để dữ liệu tĩnh (mock) hoặc lệch tên trường trên nhánh `main`, Partner chỉ cần thực hiện **6 hạng mục** dưới đây:

---

### 🛠️ Hạng mục 1: Nối API Quản lý Nhân viên (`/api/users`) vào `AdminDashboardView.tsx`

- **Hiện trạng trên nhánh `main`:**
  - Tại `src/components/AdminDashboardView.tsx` (dòng 223–273), danh sách nhân viên `staffUsers` đang khởi tạo bằng mảng 5 nhân viên cố định trong `useState`, và hàm `toggleUserActive` (dòng 325) chỉ đổi state trên bộ nhớ tạm.
- **API Backend đã có sẵn (`/api/users` — Role: `ADMIN`, `MANAGER`):**
  1. **Lấy danh sách nhân viên:** `GET /api/users`
     - Response `data`:
       ```json
       [
         {
           "id": 1,
           "tenDangNhap": "admin",
           "hoVaTen": "Administrator",
           "email": "admin@koithe.local",
           "maVaiTro": 1,
           "tenVaiTro": "ADMIN",
           "dangHoatDong": true
         }
       ]
       ```
  2. **Lấy danh sách vai trò:** `GET /api/users/roles`
     - Response `data`: `[{ "id": 1, "tenVaiTro": "ADMIN", "moTa": "..." }, ...]`
  3. **Thêm nhân viên mới:** `POST /api/users`
     - Request Body:
       ```json
       {
         "tenDangNhap": "cashier03",
         "matKhau": "Cashier@123",
         "hoVaTen": "Lê Thu Ngân 03",
         "email": "cashier03@koithe.vn",
         "maVaiTro": 3,
         "dangHoatDong": true
       }
       ```
  4. **Cập nhật thông tin nhân viên:** `PUT /api/users/{id}`
     - Request Body (để trống `matKhau` nếu không đổi mật khẩu):
       ```json
       {
         "hoVaTen": "Lê Thu Ngân 03 (Đã cập nhật)",
         "email": "cashier03@koithe.vn",
         "matKhau": "",
         "maVaiTro": 3,
         "dangHoatDong": true
       }
       ```
  5. **Khóa / Mở khóa nhanh tài khoản:** `PATCH /api/users/{id}/status`
     - Request Body: `{ "dangHoatDong": false }`
  6. **Xóa mềm nhân viên (Role `ADMIN`):** `DELETE /api/users/{id}`
- **Cách thực hiện trên POS:**
  - Tạo file `src/services/userApi.ts` (và export qua `src/services/index.ts`) gọi các endpoint trên.
  - Trong tab `staff` của `AdminDashboardView.tsx`, gọi `GET /api/users` trong `useEffect` để hiển thị danh sách thật, nối nút Khóa/Mở khóa vào `PATCH /api/users/{id}/status`, và thêm Modal Tạo/Sửa nhân viên.

---

### 🛠️ Hạng mục 2: Nối API Báo cáo Doanh thu (`/api/reports/summary`) vào `ReportView.tsx`

- **Hiện trạng trên nhánh `main`:**
  - Tại `src/components/ReportView.tsx`, số liệu báo cáo hiện đang tự cộng thủ công từ mảng `orders` truyền qua props ở phía client.
- **API Backend đã có sẵn (`GET /api/reports/summary` — Role: `ADMIN`, `MANAGER`):**
  - **Endpoint:** `GET /api/reports/summary?from=2026-11-01&to=2026-11-04&top=5` *(Nếu không truyền `from` và `to`, mặc định lấy ngày hôm nay)*.
  - **Response `data`:**
    ```json
    {
      "tuNgay": "2026-11-01",
      "denNgay": "2026-11-04",
      "tongDoanhThu": 15850000.00,
      "tongSoDonHang": 42,
      "tongSoLyDaBan": 78,
      "doanhThuTheoNgay": [
        { "ngay": "2026-11-03", "doanhThu": 1250000.00, "soDon": 15 }
      ],
      "doanhThuTheoKenh": [
        { "maKenh": "TAI_QUAY_POS", "tenKenh": "Tại quầy (POS)", "doanhThu": 10500000.00, "soDon": 28, "tyLePhanTram": 66.25 },
        { "maKenh": "ONLINE_WEB", "tenKenh": "Trực tuyến (Web / QR)", "doanhThu": 5350000.00, "soDon": 14, "tyLePhanTram": 33.75 }
      ],
      "doanhThuTheoPhuongThuc": [
        { "maPhuongThuc": "TIEN_MAT", "tenPhuongThuc": "Tiền mặt", "doanhThu": 8000000.00, "soGiaoDich": 20, "tyLePhanTram": 50.47 },
        { "maPhuongThuc": "CHUYEN_KHOAN_QR", "tenPhuongThuc": "VietQR", "doanhThu": 7850000.00, "soGiaoDich": 22, "tyLePhanTram": 49.53 }
      ],
      "topSanPhamBanChay": [
        { "bienTheId": 1, "tenSanPham": "Golden Bubble Milk Tea", "kichCo": "M", "soLuongDaBan": 35, "doanhThu": 1925000.00 }
      ]
    }
    ```
- **Cách thực hiện trên POS:**
  - Tạo `src/services/reportApi.ts` gọi `GET /api/reports/summary?from=${from}&to=${to}&top=${top}`.
  - Trong `ReportView.tsx`, khi người dùng chọn mốc thời gian (`Hôm nay`, `7 ngày qua`, `Tháng này`), quy đổi ra `from` / `to` (`YYYY-MM-DD`) và gọi API để hiển thị KPI, biểu đồ doanh thu theo ngày, tỷ trọng kênh bán và Top sản phẩm bán chạy.

---

### 🛠️ Hạng mục 3: Bổ sung CRUD Danh mục (`/api/categories`) & Sửa Sản phẩm (`PUT /api/products/{id}`)

- **Hiện trạng trên nhánh `main`:**
  - Trong `src/services/catalogApi.ts`, hiện đã có `getCategories`, `getProducts`, `createProduct`, `updateVariantStock`, `deleteProduct`, nhưng **chưa có** hàm Thêm/Sửa/Xóa Danh mục và hàm Sửa thông tin Sản phẩm (`PUT /api/products/{id}`).
- **API Backend đã có sẵn (Role: `ADMIN`, `MANAGER`):**
  1. **Tạo danh mục:** `POST /api/categories` — Body: `{ "tenDanhMuc": "Trà Trái Cây Nhiệt Đới" }`
  2. **Sửa danh mục:** `PUT /api/categories/{id}` — Body: `{ "tenDanhMuc": "Tên mới" }`
  3. **Xóa danh mục:** `DELETE /api/categories/{id}` *(Trả lỗi 409 nếu danh mục đang còn sản phẩm)*.
  4. **Cập nhật sản phẩm:** `PUT /api/products/{id}`
     - Body:
       ```json
       {
         "tenSanPham": "Golden Bubble Milk Tea",
         "maDanhMuc": 1,
         "moTa": "Trà sữa trân châu hoàng kim đặc trưng",
         "hinhAnh": "https://...",
         "noiBat": true,
         "dangBan": true
       }
       ```
  5. **Thêm biến thể Size/Giá mới cho sản phẩm:** `POST /api/products/{id}/variants`
     - Body: `{ "kichCo": "L", "giaBan": 65000, "soLuongTon": 100 }`

---

### 🛠️ Hạng mục 4: Chuẩn hóa nhận diện Kênh đặt hàng (`channel`) trong `OnlineOrdersView.tsx`

- **Hiện trạng trên nhánh `main`:**
  - Tại `src/components/OnlineOrdersView.tsx` (dòng 94 và dòng 116), hàm `getChannelInfo(order)` đang kiểm tra:
    ```tsx
    if (order.channel === 'WEB' || order.orderCode.startsWith('WEB-')) { ... }
    if (order.channel === 'QR_BAN' || order.orderCode.startsWith('QR-') || ...) { ... }
    ```
  - Trong khi đó, Backend trả về:
    - `channel: "WEBSITE"` (cho đơn khách đặt trên Website)
    - `channel: "QUET_QR_BAN"` (cho đơn khách quét mã QR tại bàn)
    - `channel: "TAI_QUAY_POS"` (cho đơn bán tại quầy POS)
    - `displayCode: "DH-2026-001"`
- **Cách sửa trong `src/components/OnlineOrdersView.tsx`:**
  - Tại **dòng 94**, thêm điều kiện `order.channel === 'WEBSITE'`:
    ```tsx
    if (order.channel === 'WEBSITE' || order.channel === 'WEB' || order.orderCode.startsWith('WEB-')) {
    ```
  - Tại **dòng 116**, thêm điều kiện `order.channel === 'QUET_QR_BAN'`:
    ```tsx
    if (order.channel === 'QUET_QR_BAN' || order.channel === 'QR_BAN' || order.orderCode.startsWith('QR-') || (order.tableId && order.orderType === 'TAI_CHO')) {
    ```
  - Tại **`src/services/orderApi.ts` (dòng 334)**, giữ nguyên `o.channel` từ Backend trả về.

---

### 🛠️ Hạng mục 5: Lấy tên Thu ngân & Tiền mặt dự kiến từ Backend trong `src/services/shiftApi.ts`

- **Hiện trạng trên nhánh `main`:**
  - Trong `src/services/shiftApi.ts` (dòng 217–222), hàm `mapBackendShift` đang gán tên thu ngân theo user đang đăng nhập trên trình duyệt (`user?.fullName || 'Thu ngân'`).
- **Cách cập nhật trong `src/services/shiftApi.ts`:**
  - Vì `CaLamViecResponse` từ Backend nay đã trả về `maThuNgan`, `tenThuNgan`, `tienMatDuKien`, `chenhLech`, hãy ưu tiên đọc trực tiếp từ `raw`:
    ```ts
    cashierName: raw.tenThuNgan || user?.fullName || 'Thu ngân',
    cashierCode: raw.maThuNgan ? `NV-0${raw.maThuNgan}` : (user?.username ? `USER-${user.username}` : `TN-${user?.id || 1}`),
    ```

---

### 🛠️ Hạng mục 6: Bỏ gán cứng `shiftId = 1` khi chưa mở ca trong `src/services/orderApi.ts`

- **Hiện trạng trên nhánh `main`:**
  - Tại `src/services/orderApi.ts` (dòng 44–46), khi không có ca nào đang mở, code đang gán dự phòng:
    ```ts
    if (!shiftId || shiftId <= 0) {
      shiftId = 1;
    }
    ```
  - Do ca `#1` trong Database đã ở trạng thái `DA_DONG`, gửi `shiftId = 1` sẽ bị Backend từ chối với lỗi `"Ca làm việc không mở hoặc không thuộc thu ngân"`.
- **Cách cập nhật trong `src/services/orderApi.ts`:**
  - Thay đoạn gán `shiftId = 1` bằng kiểm tra nhắc thu ngân mở ca rõ ràng:
    ```ts
    if (!shiftId || shiftId <= 0) {
      throw new Error('Hiện chưa có ca làm việc nào đang mở. Vui lòng mở ca làm việc (F8) trước khi thanh toán!');
    }
    ```

---

## PHẦN 4: QUY TRÌNH KIỂM THỬ LIÊN THÔNG (WEB ↔ POS ↔ DATABASE)

Sau khi cập nhật xong, Partner có thể kiểm tra toàn bộ luồng nghiệp vụ theo 4 bước:

1. **Bước 1 — Mở ca làm việc trên POS:**
   - Đăng nhập POS (`admin` / `Admin@123` hoặc `cashier01` / `Admin@123`).
   - Nhấn mở **Quản lý ca (F8)** $\rightarrow$ Nhập tiền đầu ca (VD: `1.000.000đ`) $\rightarrow$ Bấm **Mở ca**.
2. **Bước 2 — Đặt đơn từ Website hoặc tại quầy POS:**
   - **Tại quầy POS**: Chọn món, chọn Size/Đường/Đá/Topping $\rightarrow$ Thanh toán Tiền mặt hoặc VietQR $\rightarrow$ Đơn tự động chuyển sang `DA_THANH_TOAN` và trừ tồn kho ngay lập tức.
   - **Trên Website (`http://localhost:5173`)**: Khách đặt 1 đơn bất kỳ $\rightarrow$ Backend tự động gán đơn đó vào `ma_ca` đang mở trên POS.
3. **Bước 3 — Duyệt đơn Online & Pha chế (KDS) trên POS:**
   - Mở mục **Đơn Online / QR** trên POS $\rightarrow$ Đơn từ Website xuất hiện ngay trong tab **"Ca hiện tại"**.
   - Bấm chuyển trạng thái sang **Đang pha chế (`DANG_PHA_CHE`)** $\rightarrow$ **Sẵn sàng (`SAN_SANG`)** $\rightarrow$ **Hoàn thành (`HOAN_THANH`)**.
   - Backend tự động trừ kho và cộng tiền mặt đơn COD vào két tiền của ca.
4. **Bước 4 — Chốt ca cuối ngày (F8):**
   - Mở lại modal **Quản lý ca (F8)** $\rightarrow$ Kiểm tra `Tiền mặt dự kiến` (`tienMatDuKien`) đã cộng chính xác các đơn tiền mặt $\rightarrow$ Nhập tiền thực đếm và bấm **Chốt ca**.
