# Checklist Partner — MilkTea

> Mỗi module: hoàn thành nghiệp vụ, test service, thử API trên Swagger và chạy `mvn clean verify`. Ước tính/độ khó tham khảo; chốt các chính sách chưa có trong code với PO trước khi implement.

## Module Shift — Độ khó: ⭐

**Mục tiêu:** Giúp thu ngân mở ca, ghi tiền đầu ca và đóng ca để đối soát tiền mặt.

**Việc cần làm:**
- [ ] Implement `CaLamViecService` theo TODO trong ServiceImpl.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/shift/service/impl/CaLamViecServiceImpl.java` và đọc TODO ở dòng 10.

**Liên quan:** User/Auth, Order, Payment, Report.

---

## Module Table — Độ khó: ⭐

**Mục tiêu:** Quản lý bàn và QR để đơn tại chỗ gắn đúng bàn.

**Việc cần làm:**
- [ ] Implement `BanService` theo TODO trong ServiceImpl.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/table/service/impl/BanServiceImpl.java` và đọc TODO ở dòng 10.

**Liên quan:** Order, Catalog/Cart, Auth.

---

## Module Customer — Độ khó: ⭐⭐

**Mục tiêu:** Quản lý hồ sơ khách mua hàng và chuẩn bị nền tảng cho chương trình thành viên.

**Việc cần làm:**
- [ ] Implement `KhachHangService` theo TODO trong ServiceImpl; chốt chính sách xác thực/điểm/hạng với PO.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/customer/service/impl/KhachHangServiceImpl.java` và đọc TODO ở dòng 10.

**Liên quan:** Auth, Cart, Order, Promotion.

---

## Module Inventory — Độ khó: ⭐⭐

**Mục tiêu:** Ghi nhận hàng nhập từ nhà cung cấp và cộng tồn khi phiếu được duyệt.

**Việc cần làm:**
- [ ] Implement `NhaCungCapService`, `PhieuNhapHangService`, `ChiTietPhieuNhapService`; approve phải cộng tồn đúng một lần trong transaction.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/inventory/service/impl/PhieuNhapHangServiceImpl.java` và đọc TODO ở dòng 24 (duyệt phiếu ở dòng 57).

**Liên quan:** Catalog (variant/topping), User/Auth, Report.

---

## Module Promotion — Độ khó: ⭐⭐⭐

**Mục tiêu:** Tạo giảm giá theo sản phẩm, hóa đơn hoặc voucher và áp dụng chính xác khi đặt đơn.

**Việc cần làm:**
- [ ] Implement `ChuongTrinhKhuyenMaiService`, `KhuyenMaiSanPhamService`, `KhuyenMaiHoaDonService`, `KhuyenMaiVoucherService` theo TODO.
- [ ] Chốt quy tắc cộng dồn, làm tròn và quota voucher với PO.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/promotion/service/impl/ChuongTrinhKhuyenMaiServiceImpl.java` và đọc TODO ở dòng 23.

**Liên quan:** Catalog, Order, Customer.

---

## Module Payment — Độ khó: ⭐⭐⭐

**Mục tiêu:** Ghi nhận giao dịch và chỉ xác nhận đơn đã thanh toán sau khi kết quả được xác minh.

**Việc cần làm:**
- [x] Implement `GiaoDichThanhToanService` theo TODO trong ServiceImpl.
- [x] Làm callback idempotent, xác minh chữ ký/nguồn gửi; tích hợp sandbox gateway theo phạm vi được giao.
- [x] Viết Unit Test cho Service.
- [x] Test API qua Swagger.
- [x] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/payment/service/impl/GiaoDichThanhToanServiceImpl.java` và đọc TODO ở dòng 30 (điều phối gateway ở dòng 57).

**Liên quan:** Order, Report, cổng thanh toán bên ngoài.

---

## Module Report — Độ khó: ⭐⭐

**Mục tiêu:** Cho quản lý xem doanh thu và hoạt động theo thời gian từ dữ liệu đơn/giao dịch.

**Việc cần làm:**
- [ ] Implement `ReportService` theo TODO trong ServiceImpl.
- [ ] Chốt định nghĩa doanh thu, timezone và bộ lọc với PO.
- [ ] Viết Unit Test cho Service.
- [ ] Test API qua Swagger.
- [ ] Chạy `mvn clean verify` PASS.

**Gợi ý bắt đầu:** Mở `src/main/java/com/milktea/report/service/impl/ReportServiceImpl.java` và đọc TODO ở dòng 21.

**Liên quan:** Order, Payment, Shift; Inventory nếu cần báo cáo nhập kho.

---

## Thứ tự gợi ý

Shift → Table → Customer → Inventory → Promotion → Payment → Report. Đọc [03_enums_reference.md](03_enums_reference.md) trước khi bắt đầu; route scaffold có thể chưa hoạt động cho tới khi ServiceImpl được hoàn thiện.

