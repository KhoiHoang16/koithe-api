# MilkTea Backend — Partner Handover

## Tổng quan

MilkTea là backend cho hệ thống POS và đặt hàng đa kênh. Phase 1–5 đã triển khai các module nền tảng: Auth, User, Catalog, Cart và Order. Các module Payment, Promotion, Shift, Table, Customer, Inventory và Report đã có scaffolding để partner tiếp tục hoàn thiện nghiệp vụ; các TODO trong service là điểm bắt đầu, không phải nghiệp vụ đã được triển khai.

## Công nghệ

- Java 21, Spring Boot 3.3, Maven
- PostgreSQL 16, Redis 7
- Flyway quản lý schema và dữ liệu khởi tạo
- MapStruct, Spring Security, Springdoc/Swagger
- Docker Desktop và Docker Compose

## Chuẩn bị môi trường và chạy local

Cần cài Docker Desktop (đã bật Docker Compose), JDK 21 và Maven. Tại thư mục gốc dự án, sao chép cấu hình mẫu rồi khởi động các dịch vụ:

```bash
cp .env.example .env
docker compose up -d
```

Trong PowerShell có thể dùng `Copy-Item .env.example .env`. Kiểm tra trạng thái bằng `docker compose ps`; xem log bằng `docker compose logs -f backend`.

Theo cấu hình mặc định, backend trong Docker được publish tại `http://localhost:8081`, PostgreSQL tại `localhost:5433` và Redis tại `localhost:6380`. Swagger UI: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html). Nếu chạy backend trực tiếp trên máy bằng Maven, kiểm tra profile/cấu hình local trong `application-dev.yml`; cổng ứng dụng khi chạy trực tiếp có thể khác cổng Docker.

Tài khoản seed local: `admin` / `Admin@123`. Đã xác minh ngày 2026-10-01 trên database/Compose local: user tồn tại và đang hoạt động, hash có prefix BCrypt `$2a$`, API login trả HTTP 200 và access token. Đây chỉ là credential môi trường phát triển; không dùng lại trong staging/production.

## Module partner tiếp tục

Danh sách dưới đây được sắp xếp theo độ khó tăng dần tương đối:

1. **Shift** — CRUD ca làm việc, mở/đóng ca và đối soát tiền mặt.
2. **Table** — CRUD bàn, sinh QR token và đổi trạng thái bàn.
3. **Customer** — CRUD khách hàng, tích điểm và nâng hạng thành viên.
4. **Inventory** — nhập kho và cộng tồn kho khi duyệt phiếu nhập.
5. **Promotion** — bốn entity/aggregate, kiểm tra voucher và xác định thứ tự áp dụng khuyến mãi.
6. **Payment** — tích hợp VNPay/MoMo theo Strategy Pattern, xử lý callback và idempotency.
7. **Report** — truy vấn chỉ đọc, cần tối ưu query và cân nhắc read model phù hợp.

## Quy tắc phát triển

- Giữ ranh giới Modular Monolith: ưu tiên giao tiếp qua contract/facade hoặc domain event; tránh để module gọi trực tiếp implementation/service nội bộ của module khác. Các dependency hiện có cần được ghi nhận và giữ trong phạm vi tối thiểu.
- Tách từng nhà cung cấp thanh toán bằng Strategy/Adapter; không đưa logic đặc thù VNPay/MoMo vào controller hoặc service điều phối chung.
- Chụp (snapshot) tên sản phẩm, đơn giá và thông tin giá cần thiết vào chi tiết đơn hàng lúc tạo đơn; không để thay đổi catalog làm sai lịch sử đơn.
- Dùng optimistic locking cho cập nhật tồn kho và xử lý xung đột đồng thời một cách tường minh.
- Giữ API/DTO nhất quán, validate dữ liệu ở biên và bổ sung test cho cả luồng thành công lẫn quy tắc lỗi.

## Migration database

Không chỉnh sửa các migration V1–V5 đã được áp dụng. Mọi thay đổi schema/dữ liệu cần tạo migration mới tiếp theo (bắt đầu từ V6), theo đúng thứ tự và kiểm tra Flyway trên database sạch lẫn database đã nâng cấp.

## Git và review

Tạo feature branch riêng, ví dụ `feature/payment-vnpay`. Mở Pull Request mô tả phạm vi, migration/API thay đổi và cách kiểm thử; chờ review trước khi merge. Trước khi gửi PR, chạy:

```bash
mvn clean verify
```

Không đưa secret hoặc file `.env` vào commit.

## Liên hệ

Tech Lead: **[Tên bạn] — [Email]**
