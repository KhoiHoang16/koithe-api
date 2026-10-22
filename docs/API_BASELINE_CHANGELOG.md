# Nhật ký thay đổi API baseline

## Thông tin baseline

- Repository: `koithe-api`
- Branch: `integration/api-baseline`
- Commit nền tảng: `f439dc3`
- Backend: `http://localhost:8081`
- Schema database: Flyway `v9`
- Kết quả kiểm thử: 155 test, 0 failure, 0 error, 0 skipped

Tài liệu này mô tả các thay đổi backend được đưa vào baseline để frontend và partner sử dụng khi tích hợp API.

## 1. Thay đổi database

### `V7__add_delivery_address.sql`

- Bổ sung trường địa chỉ giao hàng cho đơn hàng.
- Dùng cho luồng delivery order.
- Request tạo đơn delivery cần có thông tin người nhận, số điện thoại và địa chỉ.

### `V8__add_product_image_featured.sql`

- Bổ sung ảnh sản phẩm.
- Bổ sung cờ sản phẩm nổi bật.
- Bổ sung dữ liệu giá thấp nhất từ các biến thể sản phẩm.

### `V9__add_guest_checkout_token.sql`

- Thêm cột `don_hang.guest_checkout_token` kiểu `VARCHAR(64)`.
- Thêm index có điều kiện `idx_don_hang_guest_checkout_token`.
- Token dùng để xác thực guest khi thanh toán đơn hàng.

## 2. Thay đổi catalog

### Sản phẩm

- Bổ sung `hinhAnh` và `noiBat` vào product request/response/entity.
- Response sản phẩm có thể cung cấp giá thấp nhất từ danh sách biến thể.
- Bổ sung repository projection `ProductMinPrice`.
- Các API sản phẩm chính:

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products/{id}/variants
POST   /api/products/{id}/variants
```

### Danh mục, topping và biến thể

- Chuẩn hóa mapping DTO giữa backend và frontend.
- Bổ sung cập nhật tồn kho biến thể.
- Các nhóm API đã có:

```text
/api/categories
/api/toppings
/api/products/{id}/variants
```

Frontend đã sử dụng các API catalog trong menu, product detail, cart và các trang quản trị sản phẩm.

## 3. Thay đổi order

### Request tạo đơn

- Bổ sung thông tin người nhận:
  - `khachHangTen`
  - `soDienThoai`
  - `diaChiGiaoHang`
- Delivery order bắt buộc có đủ thông tin người nhận và địa chỉ.
- Guest order lưu `guestCheckoutToken` từ cart token.

### Response đơn hàng

- Bổ sung thông tin giao hàng.
- Bổ sung dữ liệu cần thiết cho frontend hiển thị order detail và order history.
- Mapping trạng thái backend được frontend chuyển đổi sang trạng thái giao diện.

### API order chính

```text
POST   /api/orders
POST   /api/orders/pos
GET    /api/orders/my
GET    /api/orders
GET    /api/orders/{id}
PATCH  /api/orders/{id}/status
POST   /api/orders/{id}/cancel
```

### Phân quyền order

- CUSTOMER: xem đơn của chính mình.
- ADMIN/MANAGER/CASHIER: xem và xử lý danh sách đơn theo quyền.
- ADMIN/MANAGER/CASHIER/BARISTA: cập nhật trạng thái đơn.

## 4. Guest checkout và payment

### API guest COD mới

```text
POST /api/payments/guest-cash
```

Request:

```json
{
  "maDonHang": 123,
  "cartToken": "guest-cart-token",
  "soTien": 85000
}
```

Quy tắc xử lý:

- `maDonHang` là mã đơn cần thanh toán.
- `cartToken` phải trùng với token được lưu trên guest order.
- `soTien` không được âm.
- Guest chỉ được thanh toán COD/tiền mặt.
- Token không đúng trả HTTP `403`.
- Đơn đã thanh toán không được thanh toán lại.

### API payment hiện có

```text
GET  /api/payments
GET  /api/payments/{id}
POST /api/payments
POST /api/payments/guest-cash
PUT  /api/payments/{id}
DELETE /api/payments/{id}
```

Callback/gateway endpoint:

```text
GET  /api/payments/vnpay-return
GET  /api/payments/vnpay-ipn
GET  /api/payments/momo-return
POST /api/payments/momo-ipn
POST /api/payments/vietqr-webhook
```

VNPay và MoMo cần cấu hình sandbox hợp lệ. Không commit credential hoặc secret vào repository.

## 5. Security và validation

- Mở quyền public cho guest payment endpoint nhưng vẫn bắt buộc xác thực bằng cart token.
- Giữ cơ chế JWT cho các API yêu cầu đăng nhập.
- Áp dụng role guard cho order, catalog quản trị và các endpoint nghiệp vụ.
- Request guest payment dùng validation:
  - `maDonHang`: bắt buộc.
  - `cartToken`: không được rỗng.
  - `soTien`: bắt buộc và không âm.

## 6. API đã frontend sử dụng

- Auth và JWT refresh.
- Catalog: category, product, variant, topping.
- Cart guest và CUSTOMER.
- Tạo đơn, xem đơn, order history và cập nhật trạng thái.
- COD authenticated.
- Guest COD.
- Payment callback.
- Validate voucher cho CUSTOMER.
- Quản lý bàn trong các luồng đã hoàn thiện.

## 7. API backend chưa được frontend sử dụng đầy đủ

- Tra cứu đơn guest theo mã đơn và số điện thoại.
- QR order.
- Một số API khách hàng và chi tiết khách hàng.
- Nhân viên, ca làm việc và phân quyền quản trị trên giao diện.
- Kho, nhà cung cấp và phiếu nhập hàng.
- Báo cáo doanh thu và thống kê.
- Khuyến mãi nâng cao.
- Một số thao tác cấu hình hệ thống.

Các API này đã có backend controller hoặc service tương ứng; partner cần kiểm tra Swagger và DTO thực tế trước khi triển khai giao diện.

## 8. Migration và triển khai

Chạy backend bằng Docker:

```bash
docker compose up -d --build
docker compose ps
```

Kiểm tra migration:

```bash
docker compose logs --tail 100 backend
```

Chạy test local:

```bash
mvn test "-Duser.timezone=Asia/Ho_Chi_Minh"
```

Không chỉnh sửa hoặc xóa migration đã chạy trên môi trường dùng chung. Nếu cần thay đổi schema, tạo migration Flyway mới.

## 9. Giới hạn của baseline

- Voucher discount chưa được đưa đầy đủ vào contract tạo đơn.
- Payment gateway thật chưa được xác nhận với credential production/sandbox của partner.
- Browser E2E chưa được tự động hóa.
- Một số endpoint đã có backend nhưng chưa có UI frontend tương ứng.
