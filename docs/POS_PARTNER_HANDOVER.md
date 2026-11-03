# 📘 TÀI LIỆU BÀN GIAO BACKEND & HƯỚNG DẪN HOÀN THIỆN MÁY POS (`koithe-pos`)

> **Cập nhật lần cuối:** 04/11/2026  
> **Repository Backend:** `koithe-api` (Nhánh: `integration/api-baseline` & `fix/be-pending-notes` — Commit: `0a7ba97`)  
> **Repository Máy POS:** `koithe-pos` (Đối chiếu trên nhánh `main` `60d096b` & `feature/complete-11-core-features` `7277299`)  
> **Cổng chạy Backend (Docker):** `http://localhost:8081` (Swagger UI: `http://localhost:8081/swagger-ui.html`)

---

## PHẦN 1: CÁCH KÉO CODE & KHỞI CHẠY BACKEND MỚI NHẤT

> [!IMPORTANT]
> **Lưu ý cực kỳ quan trọng khi chạy Docker:**  
> Nếu chỉ chạy `docker compose up -d`, Docker sẽ **dùng lại image cũ** và **KHÔNG** biên dịch các thay đổi code Java mới. Bắt buộc phải có cờ `--build` khi cập nhật code Backend!

```bash
cd d:\koithe-api
git fetch origin
git checkout integration/api-baseline
git pull origin integration/api-baseline

# Bắt buộc thêm --build để Docker đóng gói lại file .jar mới nhất
docker compose up -d --build backend
```

---

## PHẦN 2: TỔNG HỢP NHỮNG GÌ ĐÃ THAY ĐỔI TRÊN BACKEND (`koithe-api`)

Dưới đây là 5 cụm nghiệp vụ đã được nâng cấp và chuẩn hóa trên Backend để phục vụ đồng bộ đa kênh (**Website ↔ Máy POS ↔ Quét QR tại bàn**):

### 1. Ràng buộc Ca làm việc (`CaLamViec`) khi đặt đơn & Tự động gán đơn vào Ca đang mở
- **Vấn đề trước đây:** Khách đặt hàng trên Website hoặc quét QR tạo ra đơn hàng có `ma_ca = NULL` và `ma_thu_ngan = NULL`, khiến đơn không tính vào doanh thu ca làm việc trên máy POS, đồng thời khi quán chưa mở ca khách vẫn đặt hàng được.
- **Đã thay đổi trên Backend (`OrderServiceImpl.create` & `CaLamViecServiceImpl.create`):**
  1. **Chặn đặt hàng khi chưa có ca mở:** Khi khách đặt đơn qua `POST /api/orders` (kênh `WEBSITE` hoặc `QUET_QR_BAN`), hệ thống kiểm tra bắt buộc phải có một ca làm việc đang ở trạng thái `DANG_MO`. Nếu tất cả các ca đều `DA_DONG`, API trả về `400 Bad Request`:
     > `"Hiện chưa có ca làm việc nào đang mở. Vui lòng mở ca làm việc trước khi tạo đơn hàng!"`
  2. **Tự động gán `ma_ca` và `ma_thu_ngan`:** Khi có ca đang mở (`DANG_MO`), mọi đơn hàng tạo từ Website / QR sẽ tự động được gán `ma_ca` (`shiftId`) và `ma_thu_ngan` (`cashierId`) của ca đang mở đó.
  3. **Tự động gom đơn treo khi mở ca mới:** Khi thu ngân bấm mở ca mới (`POST /api/shifts` hoặc `POST /api/shifts/open`), Backend tự động chạy query `claimUnassignedOrders` để gán toàn bộ các đơn chưa hủy mà `ma_ca IS NULL` (nếu có) vào ca vừa mở.

### 2. Đồng bộ Giao dịch Thanh toán Tiền mặt (`TIEN_MAT`) & Công thức Đối soát Ca
- **Vấn đề trước đây:**
  - Đơn khách đặt COD (`POST /api/payments/guest-cash`) tạo giao dịch `TIEN_MAT` ở trạng thái `CHO_XU_LY`. Khi thu ngân/pha chế duyệt đơn sang `DA_THANH_TOAN` hoặc `HOAN_THANH`, bản ghi `giao_dich_thanh_toan` vẫn kẹt ở `CHO_XU_LY`, dẫn đến tiền mặt dự kiến trong két (`tienMatDuKien`) không được cộng thêm.
  - Công thức `tienMatDuKien` cũ cộng toàn bộ `tongDoanhThu` (bao gồm cả đơn chuyển khoản QR/MoMo/VNPay) vào tiền mặt trong két.
- **Đã thay đổi trên Backend (`OrderServiceImpl.transition`, `GiaoDichThanhToanServiceImpl`, `CaLamViecServiceImpl`):**
  1. **Tự động chốt thu tiền mặt khi duyệt đơn:** Khi đơn hàng chuyển trạng thái sang `DA_THANH_TOAN` hoặc `HOAN_THANH` (qua `PATCH /api/orders/{id}/status`), Backend tự động cập nhật giao dịch `TIEN_MAT` đang `CHO_XU_LY` của đơn đó sang `THANH_CONG` (`markPendingCashPaymentSuccess`).
  2. **Endpoint xác nhận thu tiền mặt riêng (tùy chọn):** Bổ sung thêm:
     - `POST /api/payments/{id}/confirm-cash`
     - `POST /api/payments/confirm-cash?orderId={orderId}`
  3. **Chuẩn hóa công thức Đối soát tiền mặt (`CaLamViecResponse`):**
     - `tongDoanhThu`: Tổng tiền các đơn hợp lệ (trạng thái `DA_THANH_TOAN`, `DANG_PHA_CHE`, `SAN_SANG`, `HOAN_THANH`) thuộc ca.
     - `tienMatDuKien = tienDauCa + sumTienMatThanhCongByCaId(caId)` *(chỉ cộng các giao dịch `TIEN_MAT` có trạng thái `THANH_CONG`, không cộng nhầm đơn QR/MoMo/VNPay)*.
     - `chenhLech = tienKetCa - tienMatDuKien`.
  4. **Bổ sung thông tin thu ngân vào `CaLamViecResponse`:**
     - DTO phản hồi của `/api/shifts`, `/api/shifts/current`, `/api/shifts/{id}` nay trả thêm 2 trường:
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
  5. **Phân quyền đóng ca an toàn:** Chỉ người tạo ca (`maThuNgan == currentUser.id`) hoặc tài khoản `ADMIN` / `MANAGER` mới có quyền đóng ca (`POST /api/shifts/{id}/close` hoặc `PUT /api/shifts/{id}`). Thu ngân khác đóng ca hộ sẽ nhận lỗi `403 Forbidden`.

### 3. Hoàn thiện API Quản lý Nhân viên (`/api/users`) & Báo cáo Doanh thu (`/api/reports/summary`)
- **API Nhân viên (`/api/users`)** — Phân quyền `ADMIN`, `MANAGER`:
  - `GET /api/users`: Danh sách toàn bộ nhân viên & tài khoản.
  - `GET /api/users/roles`: Danh sách vai trò (`ADMIN`, `MANAGER`, `CASHIER`, `BARISTA`, `CUSTOMER`).
  - `POST /api/users`: Tạo tài khoản nhân viên mới.
  - `PUT /api/users/{id}`: Cập nhật họ tên, email, mật khẩu, vai trò, trạng thái.
  - `PATCH /api/users/{id}/status`: Khóa / Mở khóa nhanh tài khoản (`{ "dangHoatDong": true/false }`).
  - `DELETE /api/users/{id}`: Xóa mềm nhân viên (Role `ADMIN`).
- **API Báo cáo (`/api/reports/summary`)** — Phân quyền `ADMIN`, `MANAGER`:
  - `GET /api/reports/summary?from=YYYY-MM-DD&to=YYYY-MM-DD&top=5`: Trả về tổng doanh thu, tổng số đơn, tổng số ly đã bán, doanh thu theo ngày, doanh thu theo kênh (`TAI_QUAY_POS` vs `ONLINE_WEB`), doanh thu theo phương thức thanh toán, và top sản phẩm bán chạy.

### 4. Khôi phục Khách hàng xóa mềm & Bảo toàn lượt dùng Voucher
- **`POST /api/customers`**: Nếu tạo khách hàng có SĐT trùng với khách hàng đã bị xóa mềm trước đó, hệ thống tự động khôi phục (`restore()`) và cập nhật thông tin thay vì ném lỗi 500 Duplicate Key.
- **`PUT /api/vouchers/{id}`**: Cho phép cập nhật thông tin voucher đang diễn ra mà không bị ghi đè mất số lượt đã sử dụng (`soLuotDaDung`).

---

## PHẦN 3: RÀ SOÁT NHÁNH `main` CỦA MÁY POS (`koithe-pos`) & HƯỚNG DẪN CHO PARTNER

Qua kiểm tra mã nguồn hiện tại trên nhánh `main` (`60d096b`) và nhánh `feature/complete-11-core-features` (`7277299`) của repository **`koithe-pos`**, dưới đây là bức tranh tổng quan và **5 việc cụ thể** để Partner hoàn thiện 100% nghiệp vụ máy POS:

### 3.1. Những gì máy POS ở nhánh `main` ĐÃ LÀM TỐT (Đã nối API thật)
Trên nhánh `main`, bộ khung gọi API trong `src/services/` đã được thiết kế rất bài bản:
- **`authApi.ts`**: Đã nối `/api/auth/login`, `/api/auth/refresh`, `/api/auth/me` kèm cơ chế tự động gắn Bearer Token trong `client.ts`.
- **`catalogApi.ts`**: Đã nối `/api/categories`, `/api/products`, `/api/products/{id}/variants`, `/api/toppings`, và cập nhật tồn kho biến thể `PATCH /api/variants/{id}/stock`.
- **`orderApi.ts`**:
  - Tạo đơn tại quầy (`createPosOrder`): Gọi `POST /api/orders/pos` tạo đơn, sau đó tự động gọi tiếp `POST /api/payments` để ghi nhận thanh toán.
    - **Với Tiền mặt (`TIEN_MAT`)**: Ngay khi gọi `POST /api/payments`, Backend tự động chuyển đơn sang `DA_THANH_TOAN` và **trừ tồn kho biến thể + topping ngay lập tức**.
    - **Với Chuyển khoản QR / MoMo / VNPay**: Giữ ở `CHO_THANH_TOAN`, khi thu ngân bấm xác nhận thanh toán online (`completeOnlinePayment` gọi `POST /api/payments/dev-simulate-success`), Backend chuyển đơn sang `DA_THANH_TOAN` và **trừ tồn kho**.
  - Cập nhật trạng thái đơn (`updateOrderStatus`): Đã có logic tự động bước qua từng trạng thái hợp lệ (`CHO_XAC_NHAN` $\rightarrow$ `DA_THANH_TOAN` $\rightarrow$ `DANG_PHA_CHE` $\rightarrow$ `SAN_SANG` $\rightarrow$ `HOAN_THANH`) nếu gọi nhảy cóc bị chặn bởi State Machine.
- **`shiftApi.ts`**: Đã nối `/api/shifts/current`, `/api/shifts/open`, `/api/shifts/{id}/close`.
- **`tableApi.ts`, `customerApi.ts`, `inventoryApi.ts`, `promotionApi.ts`**: Đã nối đầy đủ các endpoint Bàn, Khách hàng thành viên, Nhà cung cấp & Phiếu nhập kho, Chương trình khuyến mãi & Voucher.

---

### 3.2. Checklist 5 điểm Partner cần cập nhật trên `koithe-pos` (Từ nhánh `main`)

#### ✅ Điểm 1: Hợp nhất (Merge) các tính năng từ nhánh `feature/complete-11-core-features` vào `main`
Trên nhánh `main` (`60d096b`), 4 tính năng quản trị trong `AdminDashboardView.tsx` và `ReportView.tsx` vẫn đang dùng dữ liệu tĩnh (mock), nhưng **đã được code hoàn chỉnh ở nhánh `feature/complete-11-core-features` (`7277299`)**, bao gồm:
1. `src/services/userApi.ts` & `src/components/StaffModal.tsx`: Quản lý nhân viên thật qua `/api/users` (thay thế mảng 5 nhân viên hardcode trong `AdminDashboardView.tsx`).
2. `src/services/reportApi.ts` & `src/components/ReportView.tsx`: Báo cáo thống kê thật qua `/api/reports/summary`.
3. `src/components/CategoryManagementModal.tsx` & `src/services/catalogApi.ts`: Thêm/Sửa/Xóa danh mục sản phẩm (`/api/categories`).
4. `src/components/EditProductModal.tsx`: Chỉnh sửa thông tin sản phẩm & quản lý biến thể Size/Giá.

👉 **Cách làm nhanh nhất cho Partner:**
```bash
cd d:\koithe-pos
git checkout main
git merge feature/complete-11-core-features
```

---

#### ✅ Điểm 2: Sửa điều kiện nhận diện Kênh đặt hàng (`channel`) trong `OnlineOrdersView.tsx` và `orderApi.ts`
- **Hiện trạng trên `main`:**
  - Tại `src/components/OnlineOrdersView.tsx` (dòng 94 & 116), code đang kiểm tra:
    ```tsx
    if (order.channel === 'WEB' || order.orderCode.startsWith('WEB-')) { ... }
    if (order.channel === 'QR_BAN' || order.orderCode.startsWith('QR-') || ...) { ... }
    ```
  - Tuy nhiên, enum chuẩn từ Backend (`OrderServiceImpl.java`) trả về:
    - `channel`: `"WEBSITE"` (chứ không phải `"WEB"`)
    - `channel`: `"QUET_QR_BAN"` (chứ không phải `"QR_BAN"`)
    - `channel`: `"TAI_QUAY_POS"`
    - `displayCode`: `"DH-2026-001"`
- **Cách sửa trong `src/components/OnlineOrdersView.tsx` (dòng 94 & 116):**
  ```tsx
  // Dòng 94: Hỗ trợ cả 'WEBSITE' và 'WEB'
  if (order.channel === 'WEBSITE' || order.channel === 'WEB' || order.orderCode.startsWith('WEB-')) {
    return {
      label: 'Đặt qua Website',
      ...
    };
  }

  // Dòng 116: Hỗ trợ cả 'QUET_QR_BAN' và 'QR_BAN'
  if (order.channel === 'QUET_QR_BAN' || order.channel === 'QR_BAN' || order.orderCode.startsWith('QR-') || (order.tableId && order.orderType === 'TAI_CHO')) {
    return {
      label: `QR ${order.tableName || ('Bàn ' + order.tableId)}`,
      ...
    };
  }
  ```
- **Và trong `src/types/pos.ts`:** Đảm bảo kiểu `channel` của `OrderRecord` cho phép `'TAI_QUAY_POS' | 'WEBSITE' | 'QUET_QR_BAN' | 'WEB' | 'APP' | 'QR_BAN' | 'ONLINE'`.

---

#### ✅ Điểm 3: Hiển thị đúng tên Thu ngân đứng ca từ Backend trong `src/services/shiftApi.ts`
- **Hiện trạng trên `main`:**
  - Trong `src/services/shiftApi.ts` (dòng 217–222), hàm `mapBackendShift` đang gán cứng tên thu ngân theo user đang đăng nhập trên trình duyệt:
    ```ts
    cashierName: user?.fullName || 'Thu ngân',
    ```
- **Cải tiến (nhờ Backend đã trả về `maThuNgan` và `tenThuNgan`):**
  - Cập nhật `mapBackendShift` trong `src/services/shiftApi.ts` để ưu tiên lấy tên thu ngân thực tế của ca từ Backend:
    ```ts
    cashierName: raw.tenThuNgan || user?.fullName || 'Thu ngân',
    cashierCode: raw.maThuNgan ? `NV-0${raw.maThuNgan}` : (user?.username ? `USER-${user.username}` : `TN-${user?.id || 1}`),
    ```
  - Đồng thời, Backend hiện đã tính sẵn `raw.tienMatDuKien` (chỉ cộng tiền mặt thực thu `THANH_CONG` + `tienDauCa`) và `raw.chenhLech`. Partner có thể dùng trực tiếp `raw.tienMatDuKien` làm `expectedCash` khi đối soát kết ca.

---

#### ✅ Điểm 4: Không fallback `shiftId = 1` khi chưa mở ca trong `src/services/orderApi.ts`
- **Hiện trạng trên `main`:**
  - Tại `src/services/orderApi.ts` (dòng 44–46), nếu không tìm thấy ca đang mở, code đang gán dự phòng:
    ```ts
    if (!shiftId || shiftId <= 0) {
      shiftId = 1;
    }
    ```
  - Vì ca `#1` trong Database đã đóng (`DA_DONG`), việc gửi `shiftId = 1` lên `POST /api/orders/pos` sẽ khiến Backend báo lỗi `"Ca làm việc không mở hoặc không thuộc thu ngân"`.
- **Cách xử lý chuẩn:**
  - Nếu `shiftId <= 0` hoặc `curShift.status !== 'DANG_MO'`, ném thông báo rõ ràng ngay trên POS để nhắc thu ngân mở ca trước khi thanh toán:
    ```ts
    if (!shiftId || shiftId <= 0) {
      throw new Error('Hiện chưa có ca làm việc nào đang mở. Vui lòng mở ca làm việc (F8) trước khi tạo đơn!');
    }
    ```

---

#### ✅ Điểm 5: Luồng duyệt đơn Online / QR trên màn hình POS (`OnlineOrdersView.tsx` & `KdsView.tsx`)
Nhờ các cập nhật mới trên Backend, luồng xử lý đơn đặt từ Website (`http://localhost:5173`) sang Máy POS (`http://localhost:3000`) hiện hoạt động liền mạch như sau:
1. **Bước 1**: Thu ngân đăng nhập POS và **Mở ca làm việc (`DANG_MO`)**.
2. **Bước 2**: Khách hàng lên Website đặt đơn (COD hoặc Chuyển khoản/MoMo/VNPay) hoặc quét QR tại bàn.
   - Backend tự động gắn đơn hàng đó vào `ma_ca` đang mở của thu ngân.
3. **Bước 3**: Trên máy POS, vào mục **Đơn Online / QR (`OnlineOrdersView`)** bấm **Làm mới**:
   - Đơn mới sẽ xuất hiện ngay trong tab **"Ca hiện tại"** vì đã có `shiftId === currentShift.id`.
4. **Bước 4**: Thu ngân bấm duyệt đơn / chuyển trạng thái sang `DA_THANH_TOAN` $\rightarrow$ `DANG_PHA_CHE` $\rightarrow$ `SAN_SANG` $\rightarrow$ `HOAN_THANH`:
   - Backend tự động trừ tồn kho biến thể & topping (khi sang `DA_THANH_TOAN`).
   - Backend tự động chuyển giao dịch `TIEN_MAT` sang `THANH_CONG` và cộng tiền vào két tiền mặt của ca đang mở.

---

## PHẦN 4: BẢNG TÓM TẮT TÀI KHOẢN TEST TRÊN HỆ THỐNG

| Vai trò | Tên đăng nhập | Mật khẩu | Quyền hạn trên POS & Web |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `Admin@123` | Toàn quyền hệ thống, mở/đóng mọi ca, quản lý nhân viên, xem báo cáo |
| **Quản lý (Manager)** | `manager01` | `Admin@123` | Quản lý menu, kho, khuyến mãi, đóng ca, xem báo cáo |
| **Thu ngân 1 (Cashier)** | `cashier01` | `Admin@123` | Mở/đóng ca của mình, bán hàng POS, duyệt đơn Online, tra cứu/tạo khách hàng |
| **Thu ngân 2 (Cashier)** | `cashier02` | `Admin@123` | Mở/đóng ca của mình, bán hàng POS, duyệt đơn Online |
| **Pha chế (Barista)** | `barista01` | `Admin@123` | Xem màn hình KDS, cập nhật tiến độ pha chế (`DANG_PHA_CHE` $\rightarrow$ `SAN_SANG`) |
