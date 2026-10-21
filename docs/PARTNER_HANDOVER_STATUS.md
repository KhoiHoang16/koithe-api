# Báo cáo bàn giao đối tác - Backend

## Thông tin nền

- Repository: `koithe-api`
- Nhánh: `develop`
- Công nghệ: Docker Compose, PostgreSQL 16, Redis 7, Java 21, Spring Boot 3.3
- Backend: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Phiên bản schema hiện tại: Flyway `v9`


## Các thay đổi đã thực hiện

### Đơn hàng và giao hàng

- Bổ sung tên người nhận, số điện thoại người nhận và địa chỉ giao hàng vào request/response/entity đơn hàng.
- Đơn giao hàng bắt buộc có tên, số điện thoại và địa chỉ.
- Bổ sung hỗ trợ ảnh sản phẩm, sản phẩm nổi bật và giá thấp nhất.
- Thêm migration `V7__add_delivery_address.sql` và `V8__add_product_image_featured.sql`.

### Guest checkout

- Thêm migration `V9__add_guest_checkout_token.sql`.
- Guest order lưu cart token làm token xác thực quyền thanh toán.
- Thêm API `POST /api/payments/guest-cash`.
- Guest chỉ được thanh toán COD/tiền mặt.
- Token sai bị từ chối với HTTP `403`.
- Đơn hàng đã thanh toán không thể thanh toán lại.
- User đã đăng nhập vẫn dùng `POST /api/payments` cho COD và thanh toán online.

### Các module đã kết nối

- Auth và JWT refresh token.
- Catalog, biến thể, topping và giá sản phẩm.
- Giỏ hàng guest và giỏ hàng user.
- Đơn giao hàng, mang đi, tại bàn và POS.
- Payment gateway và callback.
- Promotion và validate voucher.
- Bàn, ca làm việc, khách hàng, kho và báo cáo.

## Phạm vi API backend

### API đã có và đã được frontend sử dụng

- Auth, JWT và thông tin tài khoản.
- Catalog: danh mục, sản phẩm, biến thể và topping.
- Cart guest và cart CUSTOMER.
- Tạo đơn, xem đơn, lịch sử đơn và cập nhật trạng thái đơn.
- Payment COD, guest COD và payment callback.
- Voucher validation cho CUSTOMER.
- Quản lý bàn trong các luồng đã tích hợp.

### API backend đã có nhưng frontend chưa tích hợp đầy đủ

- Tra cứu đơn hàng guest theo mã đơn và số điện thoại.
- QR order.
- Chi tiết khách hàng và danh sách khách hàng trong một số màn hình admin.
- Nhân viên và phân quyền quản trị trên giao diện.
- Kho, nhà cung cấp và phiếu nhập hàng.
- Ca làm việc.
- Báo cáo doanh thu/thống kê.
- Khuyến mãi nâng cao.
- Một số thao tác cấu hình hệ thống.

Các API backend tương ứng đã tồn tại ở các controller thuộc `customer`, `inventory`, `shift`, `report` và `promotion`; phần còn thiếu chủ yếu là kết nối UI, mapping DTO và hoàn thiện thao tác mutation trên frontend.

## Kết quả kiểm thử

### Test tự động

```bash
mvn test "-Duser.timezone=Asia/Ho_Chi_Minh"
```

Kết quả: **155 test, 0 failure, 0 error, 0 skipped**.

Nếu chạy `mvn test` không có timezone, PostgreSQL local có thể từ chối alias `Asia/Saigon`. Đây là lỗi cấu hình môi trường; container và lệnh bên trên đã chạy thành công với `Asia/Ho_Chi_Minh`.

### Docker và runtime

- PostgreSQL: healthy.
- Redis: healthy.
- Backend: healthy.
- Flyway: đã chạy đến `v9`.
- `GET /api/health`: HTTP `200`.

### E2E API

- Catalog categories/products/toppings/variants: PASS.
- Đăng nhập và `/auth/me` với ADMIN, MANAGER, BARISTA, CASHIER: PASS.
- Kiểm tra RBAC: PASS.
- Guest cart token: PASS.
- Guest tạo đơn: PASS.
- Guest thanh toán COD với token đúng: PASS.
- Guest thanh toán với token sai: HTTP `403`.
- CUSTOMER cart/order/COD/history: PASS.
- BARISTA và CASHIER cập nhật trạng thái đơn: PASS.


## Giới hạn hiện tại

- Guest không được dùng voucher; API validate voucher yêu cầu CUSTOMER đã đăng nhập.
- Discount voucher chưa được đưa vào contract tạo đơn và chưa được snapshot đầy đủ vào đơn hàng.
- VNPay/MoMo cần credential sandbox và callback URL hợp lệ để test gateway thật.
- Chưa có browser E2E tự động; hiện đã xác nhận bằng API E2E, frontend proxy và build.
- Spring Data Redis có warning về việc nhận diện repository JPA. Redis vẫn healthy và dự án không kỳ vọng có Redis repository.

## Hướng dẫn chạy

```bash
docker compose up -d --build
docker compose ps
docker compose logs --tail 100 backend
```

Chạy test:

```bash
mvn test "-Duser.timezone=Asia/Ho_Chi_Minh"
```
