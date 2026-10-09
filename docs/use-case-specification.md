# ĐẶC TẢ CHỨC NĂNG HỆ THỐNG MILKTEA

## I/ Tên đề tài

**Xây dựng hệ thống bán hàng POS và đặt món đa kênh MilkTea**

## II/ Các chức năng

1. Đăng nhập hệ thống.
2. Đăng ký tài khoản khách hàng.
3. Xem thực đơn.
4. Quản lý giỏ hàng.
5. Tạo đơn hàng từ giỏ hàng.
6. Thanh toán đơn hàng.
7. Xem lịch sử và chi tiết đơn hàng.
8. Quản lý hồ sơ khách hàng.
9. Quản lý thực đơn.
10. Quản lý trạng thái đơn hàng.
11. Quản lý khuyến mãi và voucher.
12. Quản lý ca làm việc.
13. Quản lý bàn và tra cứu QR.
14. Quản lý nhà cung cấp và nhập kho.
15. Xem báo cáo.
16. Cập nhật trạng thái pha chế.
17. Quản lý tài khoản nhân viên.

## III/ Mô tả chi tiết các chức năng

### 1. Chức năng đăng nhập

**1.1. Mục đích:** Xác thực người dùng và cấp token để truy cập các chức năng theo quyền.

**1.2. Đối tượng:** CUSTOMER, CASHIER, BARISTA, MANAGER và ADMIN.

**1.3. Xử lý:** Người dùng nhập username và password. Hệ thống kiểm tra tài khoản còn hoạt động và password hợp lệ, sau đó cấp access token và refresh token.

**1.4. API và lưu ý:** `POST /api/auth/login`. Refresh token được lưu trong Redis. API hiện không trả thông tin hồ sơ người dùng trong response.

### 2. Chức năng đăng ký tài khoản khách hàng

**2.1. Mục đích:** Cho phép khách tạo tài khoản CUSTOMER.

**2.2. Đối tượng:** GUEST.

**2.3. Xử lý:** Người dùng nhập username, password, họ tên và có thể nhập số điện thoại. Hệ thống kiểm tra dữ liệu, mã hóa password rồi tạo tài khoản.

**2.4. API và lưu ý:** `POST /api/auth/register`. Backend không nhận email và chưa tạo hồ sơ khách hàng tích điểm khi đăng ký.

### 3. Chức năng xem thực đơn

**3.1. Mục đích:** Cho khách xem danh mục, sản phẩm, biến thể và topping.

**3.2. Đối tượng:** GUEST và mọi người dùng đã đăng nhập.

**3.3. Xử lý:** Người dùng xem danh sách, lọc hoặc tìm sản phẩm, rồi mở chi tiết để chọn kích cỡ và topping.

**3.4. API và lưu ý:** `GET /api/categories`, `GET /api/products`, `GET /api/products/{id}`, `GET /api/products/{id}/variants`, `GET /api/toppings`. Các API đọc thực đơn không yêu cầu đăng nhập.

### 4. Chức năng quản lý giỏ hàng

**4.1. Mục đích:** Thêm món, xem giỏ, sửa số lượng và xóa món trước khi đặt hàng.

**4.2. Đối tượng:** GUEST và CUSTOMER.

**4.3. Xử lý:** Người dùng chọn biến thể, số lượng và topping. Backend kiểm tra dữ liệu và tồn kho rồi lưu giỏ. GUEST nhận cart token để tiếp tục sử dụng giỏ.

**4.4. API và lưu ý:** `/api/carts/items`, `/api/carts/current`. Giỏ backend được lưu trên server; giao diện hiện cũng có giỏ local và dữ liệu mẫu.

### 5. Chức năng tạo đơn hàng từ giỏ

**5.1. Mục đích:** Tạo đơn hàng từ các món trong giỏ.

**5.2. Đối tượng:** GUEST, CUSTOMER và nhân viên có quyền tạo đơn.

**5.3. Xử lý:** Client gửi cart token hoặc sử dụng giỏ gắn với tài khoản, kèm loại phục vụ và kênh bán. Backend tạo đơn ở trạng thái CHO_XAC_NHAN, lưu giá tại thời điểm đặt và đánh dấu giỏ đã dùng.

**5.4. API và lưu ý:** `POST /api/orders`. Đơn tại chỗ cần mã bàn. Đơn mới chưa áp dụng voucher hoặc giảm giá; payload checkout hiện tại của frontend chưa khớp với API backend.

### 6. Chức năng thanh toán đơn hàng

**6.1. Mục đích:** Ghi nhận thanh toán tiền mặt hoặc khởi tạo thanh toán trực tuyến.

**6.2. Đối tượng:** Người dùng đã đăng nhập; cổng thanh toán VNPay, MoMo và VietQR gửi kết quả về hệ thống.

**6.3. Xử lý:** Backend kiểm tra đơn đang chờ xác nhận và số tiền bằng tổng đơn. Tiền mặt được ghi nhận ngay; thanh toán trực tuyến chờ callback. Khi thanh toán thành công, đơn chuyển sang DA_THANH_TOAN và tồn kho được cập nhật.

**6.4. API và lưu ý:** `POST /api/payments` và các callback tương ứng. GUEST có thể tạo đơn nhưng chưa thể gọi API thanh toán nếu chưa đăng nhập. Callback chưa đối chiếu số tiền nhận được với tổng đơn.

### 7. Chức năng xem lịch sử và chi tiết đơn hàng

**7.1. Mục đích:** Cho khách xem đơn của mình và nhân viên tra cứu đơn phục vụ nghiệp vụ.

**7.2. Đối tượng:** CUSTOMER; nhân viên có quyền xem danh sách hoặc chi tiết theo vai trò.

**7.3. Xử lý:** CUSTOMER xem lịch sử của mình và chỉ xem được chi tiết đơn thuộc quyền sở hữu. Nhân viên được xem danh sách hoặc chi tiết theo quyền đã cấp.

**7.4. API và lưu ý:** `GET /api/orders/my`, `GET /api/orders`, `GET /api/orders/{id}`. Chưa có chức năng cho GUEST tra cứu bằng mã đơn và số điện thoại.

### 8. Chức năng quản lý hồ sơ khách hàng

**8.1. Mục đích:** Hỗ trợ nhân viên tạo và cập nhật hồ sơ khách hàng tại cửa hàng.

**8.2. Đối tượng:** CASHIER, MANAGER và ADMIN; quyền thao tác cụ thể tùy loại hành động.

**8.3. Xử lý:** Nhân viên có quyền tìm, xem, tạo hoặc cập nhật hồ sơ khách. Quyền xóa chỉ dành cho ADMIN.

**8.4. API và lưu ý:** Các API thuộc `/api/customers`. Khách hàng chưa có API tự xem hoặc sửa hồ sơ, địa chỉ và điểm tích lũy.

### 9. Chức năng quản lý thực đơn

**9.1. Mục đích:** Quản lý danh mục, sản phẩm, biến thể và topping.

**9.2. Đối tượng:** MANAGER và ADMIN.

**9.3. Xử lý:** Người có quyền tạo và cập nhật dữ liệu thực đơn. Quyền xóa khác nhau theo loại dữ liệu; một số thao tác xóa chỉ dành cho ADMIN.

**9.4. API và lưu ý:** Các API thuộc `/api/categories`, `/api/products`, `/api/variants` và `/api/toppings`. API đọc catalog là công khai; chưa có API tải ảnh sản phẩm.

### 10. Chức năng quản lý trạng thái đơn hàng

**10.1. Mục đích:** Theo dõi và cập nhật tiến độ xử lý đơn.

**10.2. Đối tượng:** CASHIER, BARISTA, MANAGER và ADMIN; CUSTOMER có thể hủy đơn của mình khi trạng thái cho phép.

**10.3. Xử lý:** Đơn đi theo trạng thái CHO_XAC_NHAN → DA_THANH_TOAN → DANG_PHA_CHE → SAN_SANG → HOAN_THANH. Đơn chưa kết thúc có thể được hủy theo quy tắc của hệ thống.

**10.4. API và lưu ý:** `GET /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/status`, `POST /api/orders/{id}/cancel`. Khi thanh toán, hệ thống trừ tồn; khi hủy đơn đã trừ tồn, hệ thống hoàn tồn. CASHIER có thể đổi trạng thái sang hủy qua API trạng thái nhưng không được gọi API hủy riêng.

### 11. Chức năng quản lý khuyến mãi và voucher

**11.1. Mục đích:** Quản lý chương trình giảm giá, quy tắc khuyến mãi và mã voucher.

**11.2. Đối tượng:** MANAGER và ADMIN quản lý cấu hình; CUSTOMER và một số nhân viên có thể kiểm tra hoặc áp dụng voucher.

**11.3. Xử lý:** Hệ thống kiểm tra thời hạn, điều kiện đơn hàng và số lượt còn lại trước khi áp dụng voucher.

**11.4. API và lưu ý:** Các API thuộc `/api/promotion-programs`, `/api/product-promotions`, `/api/invoice-promotions` và `/api/vouchers`. Các API này cần đăng nhập. Khuyến mãi hiện chưa được nối vào quy trình tạo đơn.

### 12. Chức năng quản lý ca làm việc

**12.1. Mục đích:** Ghi nhận mở ca, đóng ca và số liệu tổng kết ca.

**12.2. Đối tượng:** CASHIER; MANAGER và ADMIN có các quyền quản lý/xem theo API.

**12.3. Xử lý:** Thu ngân mở ca với tiền đầu ca, xem ca hiện tại rồi đóng ca với tiền kết ca. Backend tính tổng đơn gắn với ca và chênh lệch.

**12.4. API và lưu ý:** Các API thuộc `/api/shifts`. Tổng hiện tại cộng tiền các đơn gắn với ca, chưa lọc riêng đơn đã hoàn tất hoặc phương thức tiền mặt.

### 13. Chức năng quản lý bàn và tra cứu QR

**13.1. Mục đích:** Quản lý bàn, trạng thái bàn và tra cứu bàn bằng mã QR.

**13.2. Đối tượng:** MANAGER và ADMIN quản lý bàn; CASHIER cập nhật trạng thái theo quyền; GUEST có thể tra cứu bàn bằng QR.

**13.3. Xử lý:** Nhân viên tạo hoặc cập nhật bàn và mã QR. Khi khách quét mã, backend trả thông tin bàn tương ứng.

**13.4. API và lưu ý:** Các API thuộc `/api/tables`, gồm `GET /api/tables/qr/{token}`. Tra cứu QR hiện chưa tạo phiên gọi món hoặc đơn hàng.

### 14. Chức năng quản lý nhà cung cấp và nhập kho

**14.1. Mục đích:** Quản lý nhà cung cấp, lập phiếu nhập và cập nhật tồn kho.

**14.2. Đối tượng:** MANAGER và ADMIN.

**14.3. Xử lý:** Nhân viên tạo phiếu nhập, thêm các mặt hàng và gửi duyệt. Khi duyệt phiếu hợp lệ, hệ thống cộng số lượng vào tồn kho.

**14.4. API và lưu ý:** Các API thuộc `/api/suppliers`, `/api/purchase-orders` và `/api/purchase-order-lines`. Phiếu mới ở trạng thái CHO_DUYET; mã phiếu được tạo từ UUID. Giao diện quản lý kho hiện chưa hoàn thiện.

### 15. Chức năng xem báo cáo

**15.1. Mục đích:** Cung cấp số liệu tổng hợp phục vụ quản lý cửa hàng.

**15.2. Đối tượng:** MANAGER và ADMIN theo mục tiêu nghiệp vụ.

**15.3. Xử lý hiện tại:** `GET /api/reports` trả response rỗng; chưa có truy vấn doanh thu, sản phẩm bán chạy hoặc tổng kết ca.

**15.4. Trạng thái:** Chức năng báo cáo chưa triển khai nghiệp vụ; trang frontend cũng đang là placeholder.

### 16. Chức năng cập nhật trạng thái pha chế

**16.1. Mục đích:** Cập nhật tiến độ pha chế và giao đơn.

**16.2. Đối tượng:** BARISTA; một số vai trò nhân viên khác cũng được phép cập nhật trạng thái qua API chung.

**16.3. Xử lý:** Khi biết mã đơn, BARISTA xem chi tiết và chuyển đơn lần lượt sang DANG_PHA_CHE, SAN_SANG rồi HOAN_THANH.

**16.4. API và lưu ý:** `GET /api/orders/{id}` và `PATCH /api/orders/{id}/status`. Chưa có API hàng đợi riêng cho BARISTA và chưa có màn hình KDS hoàn chỉnh.

### 17. Chức năng quản lý tài khoản nhân viên

**17.1. Mục đích:** Quản lý tài khoản và vai trò nhân viên.

**17.2. Đối tượng:** ADMIN.

**17.3. Trạng thái hiện tại:** Chưa có API quản lý người dùng để tạo, sửa, khóa tài khoản hoặc gán vai trò. Trang nhân viên trên frontend đang là placeholder.

**17.4. Lưu ý:** API đăng ký hiện chỉ tạo tài khoản CUSTOMER, nên không thay thế chức năng quản lý nhân viên.

