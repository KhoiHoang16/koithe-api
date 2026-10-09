# 📋 ĐẶC TẢ CHỨC NĂNG — DỰ ÁN MILKTEA

> Tài liệu mô tả chức năng theo source code hiện có của backend và giao diện Customer Web/Admin Dashboard.
> Phiên bản: 1.0 | Ngày: 09/10/2026
>
> **Quy ước:** “Endpoint” được đếm theo tổ hợp HTTP method + path. Hai method POST/PATCH trên cùng route duyệt phiếu nhập được tính là hai endpoint nhưng cùng một handler/nghiệp vụ. API chưa tồn tại hoặc đang scaffold được đánh dấu rõ, không suy diễn từ đặc tả mẫu.

---

## 📖 MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Danh sách chức năng theo module](#2-danh-sách-chức-năng-theo-module)
3. [Đặc tả chi tiết từng chức năng (IPO)](#3-đặc-tả-chi-tiết-từng-chức-năng-ipo)
4. [Ma trận phân quyền](#4-ma-trận-phân-quyền)
5. [Business Rules toàn hệ thống](#5-business-rules-toàn-hệ-thống)
6. [Phạm vi giao diện và trạng thái triển khai](#6-phạm-vi-giao-diện-và-trạng-thái-triển-khai)
7. [Tổng kết và căn cứ source code](#7-tổng-kết-và-căn-cứ-source-code)

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1. Mục tiêu

MilkTea là backend REST cho nghiệp vụ bán hàng, đặt món đa kênh và quản trị cửa hàng. Source hiện có hỗ trợ đăng nhập, menu, giỏ hàng, tạo đơn từ giỏ/POS, thanh toán qua nhiều adapter, khuyến mãi/voucher, quản lý ca/bàn/khách hàng và nhập kho. Báo cáo tổng hợp hiện chưa có dữ liệu nghiệp vụ trả về.

Phạm vi tài liệu gồm backend REST cùng các route Customer Web và Admin Dashboard có trong frontend. Màn hình POS chuyên dụng và KDS không nằm trong phạm vi đồ án này.

### 1.2. Kiến trúc và công nghệ theo repository

| Thành phần | Công nghệ / phạm vi |
|:---|:---|
| Backend | Java 21, Spring Boot 3.3.12, Spring Security, Spring Data JPA, Flyway |
| Dữ liệu | PostgreSQL 16, Redis 7; V1–V5 tạo 24 bảng (V6 chỉ seed dữ liệu thử nghiệm) |
| Giao diện | React 19, Vite, TypeScript; có Customer Web và Admin Dashboard |
| API | REST dưới `/api`; response nghiệp vụ thường bọc trong `ApiResponse`, danh sách phân trang dùng `PageResponse` |
| Phân quyền | JWT stateless; method security với `@EnableMethodSecurity`; quyền endpoint được kết hợp giữa `SecurityConfig`, `@PreAuthorize` và kiểm tra trong service |

### 1.3. Đối tượng sử dụng

| Role / Actor | Mô tả | Có trong seed DB |
|:---|:---|:---:|
| **GUEST** | Request không có JWT; có thể xem các API public và sử dụng cart token | Không; là trạng thái chưa đăng nhập |
| **CUSTOMER** | Tài khoản đăng ký qua `/api/auth/register`, dùng chức năng khách hàng | Có |
| **CASHIER** | Nhân viên thu ngân | Có |
| **BARISTA** | Nhân viên pha chế | Có |
| **MANAGER** | Quản lý | Có |
| **ADMIN** | Quản trị hệ thống | Có |

Database seed V2 tạo **5 role có tên**: ADMIN, MANAGER, CASHIER, BARISTA, CUSTOMER. GUEST không phải role/JWT authority trong DB.

### 1.4. Sơ đồ kiến trúc

```text
┌─────────────────────────────┐      ┌──────────────────────────────┐
│ Customer Web                │      │ Admin Dashboard              │
│ menu, cart, checkout, orders│      │ catalog, orders, quản trị    │
└─────────────┬───────────────┘      └────────────┬─────────────────┘
              └────────────────┬──────────────────┘
                               │ HTTPS / REST
                               ▼
                    ┌───────────────────────┐
                    │ Spring Boot API       │
                    │ modular monolith      │
                    └──────┬────────┬───────┘
                           │        │
                    ┌──────▼───┐ ┌──▼─────┐      ┌─────────────────┐
                    │PostgreSQL│ │ Redis  │      │ Payment gateways│
                    │   16     │ │   7    │      │ VNPay/MoMo/QR   │
                    └──────────┘ └────────┘      └─────────────────┘
```

### 1.5. Thống kê đã đối chiếu

| Hạng mục | Số lượng / ghi chú |
|:---|:---:|
| Module nghiệp vụ được yêu cầu | 12 |
| Endpoint backend trong 12 module | 107 method + path |
| Endpoint phụ trợ health | 1 (`GET /api/health`), ngoài 12 module |
| IPO endpoint rows trong tài liệu | 107 |
| Role lưu trong database seed | 5 (GUEST là anonymous actor) |
| Bảng tạo bởi V1 và V5 | 24 |
| REST endpoint cho module User | 0; mới có entity/repository |

---

## 2. DANH SÁCH CHỨC NĂNG THEO MODULE

| Module | Endpoint | Chức năng có trong code | Actor/quyền chính |
|:---|---:|:---|:---|
| auth | 5 | Đăng nhập, đăng ký CUSTOMER, refresh, logout, xem user hiện tại | Public; `/me` cần xác thực |
| user | 0 | Entity/repository cho người dùng và vai trò; chưa có CRUD API | Đăng ký qua auth gán CUSTOMER |
| catalog | 18 | Danh mục, sản phẩm, biến thể/giá/tồn, topping | GET công khai; ghi ADMIN/MANAGER; xóa đa số chỉ ADMIN |
| cart | 5 | Thêm/xem/sửa/xóa món và xóa cart; guest token, tự gộp cart khi CUSTOMER truy cập | `/api/carts/**` public; logic nhận diện CUSTOMER ở service |
| order | 7 | Tạo đơn online/QR từ cart, POS, truy vấn, chuyển trạng thái, hủy | Public tạo từ cart; POS và truy vấn theo role/owner |
| payment | 11 | Giao dịch, khởi tạo tiền mặt/online, callback VNPay/MoMo/VietQR, mô phỏng dev | CRUD cần JWT; callback và mô phỏng được permitAll |
| promotion | 23 | CRUD campaign/rule/voucher; validate, apply và release quota voucher | GET cần JWT; ghi theo `@PreAuthorize`; validate cần JWT |
| shift | 8 | Danh sách, mở, xem, sửa/đóng, xóa ca và tính tổng hợp doanh thu ca | ADMIN/MANAGER/CASHIER theo endpoint |
| table | 7 | CRUD bàn, trạng thái bàn, tra cứu bàn bằng QR | QR public; CRUD theo role |
| customer | 5 | Tìm, xem, tạo, cập nhật, xóa mềm hồ sơ khách hàng | CASHIER/MANAGER/ADMIN theo endpoint |
| inventory | 17 | Nhà cung cấp, phiếu nhập và dòng hàng; duyệt phiếu cộng kho | Đọc cần JWT; ghi chủ yếu ADMIN/MANAGER |
| report | 1 | Route summary hiện trả `success(null)`; chưa có báo cáo | JWT bất kỳ role do security fallback |

Endpoint count đã tính cả POST và PATCH tại `/api/purchase-orders/{id}/approve`. Endpoint phụ trợ `GET /api/health` không tính vào 12 module.

---

## 3. ĐẶC TẢ CHI TIẾT TỪNG CHỨC NĂNG (IPO)

Mỗi dòng dưới đây là một endpoint (hoặc một trong hai method của route duyệt phiếu nhập). “Authenticated” nghĩa là request phải có JWT hợp lệ; nếu route không có `@PreAuthorize`, các role đã đăng nhập được phép qua cấu hình mặc định, trừ khi service kiểm tra thêm.

### 3.1. MODULE AUTH — Xác thực (5 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-AUTH-01 — POST `/api/auth/login`**<br>Actor: tất cả tài khoản; public.<br>`username`, `password` bắt buộc. | Tìm `NguoiDung` theo username; từ chối nếu không tồn tại, bị vô hiệu hóa hoặc password BCrypt không khớp; tạo access JWT và refresh JWT; lưu refresh token trong Redis theo key `auth:refresh:{token}` với TTL cấu hình. | HTTP 200; `accessToken`, `refreshToken`, `tokenType: Bearer`. Sai tài khoản hoặc password trả 401 chung. |
| **FR-AUTH-02 — POST `/api/auth/register`**<br>Actor: GUEST; public.<br>`username` 3–100 ký tự, `password` 8–100 ký tự, `fullName` bắt buộc; `phone` tùy chọn. | Kiểm tra username chưa tồn tại; tìm role CUSTOMER; BCrypt password; tạo user hoạt động; phát và lưu JWT như login. Tài khoản CUSTOMER mới được tạo trong `nguoi_dung`, chưa tạo `khach_hang` tại bước này. | HTTP 201; access/refresh token và token type. Username trùng trả 409; thiếu role CUSTOMER là lỗi hệ thống. |
| **FR-AUTH-03 — POST `/api/auth/refresh`**<br>Actor: người có refresh token; public route nhưng token phải hợp lệ.<br>`refreshToken` bắt buộc. | Kiểm tra token đúng loại refresh và còn key Redis; lấy subject username; tìm user; xóa token cũ rồi cấp cặp token mới (rotation). | HTTP 200; access token và refresh token mới. Token không hợp lệ/hết hạn/đã thu hồi trả 401. |
| **FR-AUTH-04 — POST `/api/auth/logout`**<br>Actor: client có thể gửi refresh token; public.<br>Body có thể rỗng; nếu có dùng `refreshToken`. | Nếu body có token, xóa Redis key tương ứng. Không có token thì không có thao tác thu hồi. Access token không bị blacklist. | HTTP 204; không có body. |
| **FR-AUTH-05 — GET `/api/auth/me`**<br>Actor: mọi tài khoản đã đăng nhập.<br>JWT trong Authorization header. | Lấy user id từ principal; tải user kèm vai trò; ánh xạ thông tin cơ bản, không trả hash mật khẩu. | HTTP 200; `id`, `username`, `fullName`, `role`; user không còn tồn tại trả 404. |

### 3.2. MODULE USER — Tài khoản và vai trò (0 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-USER-01 — Mô hình người dùng/vai trò, chưa có API quản trị**<br>Actor: không có CRUD endpoint. | `NguoiDung` liên kết `VaiTro`; username unique, lưu BCrypt hash, cờ `dangHoatDong`; role được seed từ V2. Đăng ký CUSTOMER nằm ở auth. Không có `UserController`, API tạo/sửa/xóa nhân viên hoặc CRUD vai trò. | Chỉ các API auth hiện có trả token/profile. Admin Dashboard có route `/admin/employees` nhưng page hiện là placeholder; không thể xem như CRUD nhân viên đã triển khai. |

### 3.3. MODULE CATALOG — Thực đơn, sản phẩm và tồn kho (18 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-CAT-01 — GET `/api/categories`**<br>Actor: public.<br>Không có input. | Lấy các danh mục chưa xóa mềm, sắp xếp tên tăng dần. | HTTP 200; danh sách `id`, tên danh mục, thời điểm tạo/cập nhật. |
| **FR-CAT-02 — GET `/api/categories/{id}`**<br>Actor: public.<br>`id` trên path. | Tải danh mục chưa xóa mềm theo id. | HTTP 200; thông tin danh mục; 404 nếu không tồn tại. |
| **FR-CAT-03 — POST `/api/categories`**<br>Actor: ADMIN, MANAGER.<br>`tenDanhMuc` không rỗng, tối đa 255 ký tự. | Trim tên; tạo entity danh mục. Code không kiểm tra trùng tên ở service và schema không có unique constraint cho tên. | HTTP 201; danh mục vừa tạo. |
| **FR-CAT-04 — PUT `/api/categories/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`, `tenDanhMuc`. | Tìm danh mục còn hiệu lực; cập nhật và trim tên. | HTTP 200; danh mục sau cập nhật; 404 nếu không tìm thấy. |
| **FR-CAT-05 — DELETE `/api/categories/{id}`**<br>Actor: ADMIN.<br>`id`. | Từ chối 409 nếu còn sản phẩm chưa xóa thuộc danh mục; nếu không thì đánh dấu `deleted_at`. | HTTP 204; danh mục bị ẩn khỏi truy vấn thường. |
| **FR-CAT-06 — GET `/api/products`**<br>Actor: public.<br>Filter tùy chọn `categoryId`, `keyword`; `page/size/sort` qua Pageable, mặc định size 20, id tăng dần. | Lọc sản phẩm chưa xóa; nếu có thì lọc danh mục và tìm keyword không phân biệt hoa thường trong tên; truy vấn phân trang. | HTTP 200; `PageResponse<ProductResponse>` gồm content và metadata phân trang. |
| **FR-CAT-07 — GET `/api/products/{id}`**<br>Actor: public.<br>`id`. | Tải sản phẩm chưa xóa mềm. | HTTP 200; id danh mục/tên/mô tả; 404 nếu không thấy. Biến thể đọc qua endpoint riêng. |
| **FR-CAT-08 — POST `/api/products`**<br>Actor: ADMIN, MANAGER.<br>`maDanhMuc`, `tenSanPham` bắt buộc; `moTa` tùy chọn. | Kiểm tra danh mục tồn tại và chưa xóa; trim tên; tạo sản phẩm. | HTTP 201; sản phẩm mới. |
| **FR-CAT-09 — PUT `/api/products/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`, `maDanhMuc`, `tenSanPham`, `moTa`. | Tải sản phẩm còn hiệu lực; kiểm tra danh mục đích; cập nhật tên/mô tả/danh mục. | HTTP 200; sản phẩm sau cập nhật; 404 nếu sản phẩm hoặc danh mục không tồn tại. |
| **FR-CAT-10 — DELETE `/api/products/{id}`**<br>Actor: ADMIN.<br>`id`. | Đánh dấu sản phẩm xóa mềm. Service không tự kiểm tra biến thể/đơn liên quan trước khi xóa mềm. | HTTP 204. |
| **FR-CAT-11 — GET `/api/products/{id}/variants`**<br>Actor: public.<br>`id` sản phẩm. | Kiểm tra sản phẩm còn hiệu lực; lấy các biến thể chưa xóa mềm, theo id tăng dần. | HTTP 200; danh sách size, giá, tồn và cờ còn hàng. |
| **FR-CAT-12 — POST `/api/products/{id}/variants`**<br>Actor: ADMIN, MANAGER.<br>`kichCo`, `giaBan > 0`, `soLuongTon >= 0` tùy chọn. | Kiểm tra sản phẩm; trim size; từ chối trùng size không phân biệt hoa thường trong cùng sản phẩm; khởi tạo cờ còn hàng theo tồn nếu tồn được gửi. | HTTP 201; biến thể mới. Trùng size trả 409. |
| **FR-CAT-13 — PUT `/api/variants/{id}`**<br>Actor: ADMIN, MANAGER.<br>`kichCo`, `giaBan > 0`, `soLuongTon` tùy chọn. | Tải biến thể; kiểm tra size mới không trùng với biến thể khác trong cùng sản phẩm; cập nhật giá/size và tồn nếu có; cập nhật cờ hàng theo tồn. | HTTP 200; biến thể sau cập nhật; trùng size trả 409. |
| **FR-CAT-14 — PATCH `/api/variants/{id}/stock`**<br>Actor: ADMIN, MANAGER.<br>`soLuongTon >= 0`. | Cập nhật tồn; tự đặt `conHang = soLuongTon > 0`; entity có `@Version` để optimistic locking. | HTTP 200; biến thể sau cập nhật. |
| **FR-CAT-15 — GET `/api/toppings`**<br>Actor: public.<br>Không có filter trong controller. | Lấy topping chưa xóa, sắp xếp theo tên. | HTTP 200; danh sách id, tên, giá, tồn, cờ còn hàng. |
| **FR-CAT-16 — POST `/api/toppings`**<br>Actor: ADMIN, MANAGER.<br>Tên bắt buộc; `giaBan >= 0.01`; `soLuongTon >= 0` tùy chọn. | Trim tên; tạo topping; nếu có tồn thì suy ra cờ còn hàng từ tồn dương. | HTTP 201; topping vừa tạo. |
| **FR-CAT-17 — PUT `/api/toppings/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`, tên, giá, tồn tùy chọn. | Tìm topping còn hiệu lực; cập nhật tên/giá; cập nhật tồn và cờ còn hàng nếu tồn được gửi. | HTTP 200; topping sau cập nhật; 404 nếu không tồn tại. |
| **FR-CAT-18 — DELETE `/api/toppings/{id}`**<br>Actor: ADMIN.<br>`id`. | Đánh dấu topping xóa mềm. | HTTP 204. |

### 3.4. MODULE CART — Giỏ hàng (5 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-CART-01 — POST `/api/carts/items`**<br>Actor: GUEST/CUSTOMER; route public.<br>Header `X-Cart-Token` tùy chọn; `variantId`, `quantity >= 1`, đường/đá/note tùy chọn, danh sách topping id + quantity. | Resolve cart theo khách hoặc UUID token; với CUSTOMER thì gắn cart khách và tự chuyển các dòng guest cart vào cart khách khi resolve; kiểm tra biến thể/topping còn hàng và tồn đủ; lưu dòng và topping; tính lại tiền từ giá hiện hành phía server. | HTTP 201; cart và tổng tiền; trả header `X-Cart-Token` cho giỏ guest. Thiếu/hết tồn trả lỗi nghiệp vụ (thường 409). |
| **FR-CART-02 — GET `/api/carts/current`**<br>Actor: GUEST/CUSTOMER; route public.<br>`X-Cart-Token` tùy chọn; JWT nếu là CUSTOMER. | Tìm hoặc tạo cart guest theo UUID; CUSTOMER được tìm/tạo cart theo hồ sơ `KhachHang` và có thể merge cart guest khi truy cập. | HTTP 200; các dòng giỏ, topping, giá tính lại và tổng tiền; header `X-Cart-Token` nếu là guest. |
| **FR-CART-03 — PATCH `/api/carts/items/{id}`**<br>Actor: GUEST/CUSTOMER; route public.<br>`quantity` mới (tùy chọn, nếu gửi phải >=1), `sugarLevel`, `iceLevel` tùy chọn; token/principal xác định chủ giỏ. | Resolve cart; xác nhận dòng thuộc cart; nếu đổi số lượng thì kiểm tra tồn biến thể; cập nhật quantity/đường/đá và tính lại tổng. Request hiện không hỗ trợ sửa danh sách topping hoặc note. | HTTP 200; toàn bộ cart và tổng tiền mới; 404 nếu dòng không thuộc giỏ/không tồn tại. |
| **FR-CART-04 — DELETE `/api/carts/items/{id}`**<br>Actor: GUEST/CUSTOMER; route public.<br>`id`, `X-Cart-Token` hoặc JWT. | Resolve cart; xác nhận dòng thuộc cart; xóa mềm dòng và các topping của dòng. | HTTP 204. |
| **FR-CART-05 — DELETE `/api/carts/current`**<br>Actor: GUEST/CUSTOMER; route public.<br>`X-Cart-Token` hoặc JWT. | Resolve cart; đánh dấu xóa mềm toàn bộ dòng và topping còn hiệu lực. | HTTP 204. |

### 3.5. MODULE ORDER — Đơn hàng (7 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-ORD-01 — POST `/api/orders`**<br>Actor: public GUEST/CUSTOMER; có thể staff nếu chỉ định customer hợp lệ.<br>`cartToken` hoặc CUSTOMER cart; `customerId` tùy chọn; `serviceType`, `channel`, `tableId` tùy chọn theo request. | Validate service type `TAI_CHO`, `MANG_VE`, `GIAO_HANG`; channel `TAI_QUAY_POS`, `QUET_QR_BAN`, `UNG_DUNG_APP`, `WEBSITE`; TAI_CHO cần bàn. Lấy cart không rỗng; kiểm tra món/topping/tồn; tạo mã đơn; copy giá biến thể/topping và tùy chọn vào dòng order; tổng giảm hiện đặt 0; lưu đơn `CHO_XAC_NHAN`; xóa mềm cart sau khi tạo. CUSTOMER gắn với hồ sơ khách bằng SĐT; guest dùng cart token. | HTTP 201; mã đơn, trạng thái, kênh/loại phục vụ, tổng tiền và các dòng snapshot. Không có áp dụng promotion/voucher trong luồng tạo đơn hiện tại. |
| **FR-ORD-02 — POST `/api/orders/pos`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`cashierId`, `shiftId`, loại phục vụ; danh sách item (variant, quantity, đường/đá/note/topping); `customerId`/`tableId` tùy chọn. | Validate loại phục vụ và bàn nếu TAI_CHO; kiểm tra thu ngân đang hoạt động, ca thuộc thu ngân và đang mở; xác thực khách nếu có; kiểm tra tồn; tạo đơn kênh `TAI_QUAY_POS`, lưu snapshot giá và tổng tiền. | HTTP 201; chi tiết order POS với trạng thái ban đầu `CHO_XAC_NHAN`. |
| **FR-ORD-03 — GET `/api/orders/my`**<br>Actor: CUSTOMER.<br>Phân trang Pageable, mặc định 20 dòng, mới nhất trước. | Tìm hồ sơ khách theo tài khoản hiện tại/SĐT; truy vấn các đơn của khách chưa xóa. | HTTP 200; trang lịch sử đơn của khách. |
| **FR-ORD-04 — GET `/api/orders`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`status`, `channel` tùy chọn; phân trang mặc định 20, mới nhất trước. | Kiểm tra status thuộc state machine và channel thuộc tập kênh; truy vấn/lọc danh sách chưa xóa. | HTTP 200; `PageResponse<OrderResponse>`; filter không hợp lệ trả 400. |
| **FR-ORD-05 — GET `/api/orders/{id}`**<br>Actor: ADMIN, MANAGER, CASHIER, BARISTA hoặc CUSTOMER là chủ đơn; cần JWT.<br>`id`. | Tải order; cho phép staff trong nhóm trên hoặc kiểm tra order thuộc hồ sơ CUSTOMER hiện tại; từ chối vai trò khác/khách không phải chủ. | HTTP 200; order và các dòng snapshot; 401/403/404 theo trường hợp. |
| **FR-ORD-06 — PATCH `/api/orders/{id}/status`**<br>Actor: ADMIN, MANAGER, CASHIER, BARISTA.<br>`status` bắt buộc trong `CHO_XAC_NHAN, DA_THANH_TOAN, DANG_PHA_CHE, SAN_SANG, HOAN_THANH, DA_HUY`. | Kiểm tra chuyển trạng thái hợp lệ; chuyển sang DA_THANH_TOAN thì trừ tồn; chuyển sang DA_HUY thì hoàn tồn nếu trước đó đã trừ. Các phép trừ tồn chạy trong transaction; variant/topping có `@Version`. | HTTP 200; order sau chuyển trạng thái; chuyển trạng thái sai trả 400, tồn không đủ trả 409. |
| **FR-ORD-07 — POST `/api/orders/{id}/cancel`**<br>Actor: ADMIN/MANAGER hoặc CUSTOMER là chủ đơn; cần JWT.<br>`id`. | Kiểm tra quyền sở hữu/role; dùng state machine chuyển DA_HUY; hoàn tồn nếu đã bị trừ. CASHIER/BARISTA không được hủy qua service này. | HTTP 200; order đã hủy; order hoàn thành/hủy trước đó không thể hủy lại. |

### 3.6. MODULE PAYMENT — Thanh toán (11 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-PAY-01 — GET `/api/payments`**<br>Actor: mọi role đã đăng nhập.<br>Không có filter. | Đọc các giao dịch chưa xóa mềm. | HTTP 200; danh sách giao dịch. |
| **FR-PAY-02 — GET `/api/payments/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id`. | Tải giao dịch chưa xóa mềm. | HTTP 200; chi tiết giao dịch; 404 nếu không tìm thấy. |
| **FR-PAY-03 — POST `/api/payments`**<br>Actor: mọi role đã đăng nhập (chưa giới hạn riêng bằng `@PreAuthorize`).<br>`maDonHang`, phương thức, `soTien >= 0`; `noiDungVietqr` tùy chọn. | Tìm order và yêu cầu trạng thái CHO_XAC_NHAN; tiền phải bằng chính xác tổng thanh toán (đơn 0 đồng chỉ nhận 0); chọn gateway theo method. Tiền mặt thành công ngay và chuyển order sang DA_THANH_TOAN; online tạo giao dịch CHO_XU_LY, gọi adapter và trả redirect URL/QR URL nếu có. | HTTP 200; giao dịch với trạng thái, số tiền, method, thời gian và có thể có `redirectUrl`. Method không hỗ trợ hoặc order không hợp lệ trả lỗi nghiệp vụ. |
| **FR-PAY-04 — PUT `/api/payments/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id` và body giao dịch theo DTO (order, method, amount, nội dung, trạng thái/thời gian). | Controller nhận request nhưng service chủ động từ chối chỉnh sửa giao dịch trực tiếp. | HTTP 400 với thông báo không hỗ trợ cập nhật. |
| **FR-PAY-05 — DELETE `/api/payments/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id`. | Tìm giao dịch; không cho xóa mềm giao dịch THANH_CONG; giao dịch khác được đánh dấu deleted. | HTTP 200 với `ApiResponse.success(null)` khi xóa được; 400 nếu đã thanh toán thành công; 404 nếu không tồn tại. |
| **FR-PAY-06 — GET `/api/payments/vnpay-return`**<br>Actor: VNPay/browser return; public.<br>Toàn bộ VNPay callback query parameters. | Adapter kiểm tra chữ ký HMAC-SHA512; đọc kết quả VNPay; dịch mã tham chiếu về order và cập nhật giao dịch/trạng thái order. | HTTP 200 với giao dịch đã xử lý; chữ ký/kết quả lỗi tạo trạng thái thất bại theo service. |
| **FR-PAY-07 — GET `/api/payments/vnpay-ipn`**<br>Actor: webhook VNPay; public.<br>Query parameters callback. | Cùng luồng xác thực signature và cập nhật giao dịch như VNPay return; xử lý callback lặp khi order đã thanh toán. | HTTP 200; giao dịch tương ứng. |
| **FR-PAY-08 — GET `/api/payments/momo-return`**<br>Actor: MoMo/browser return; public.<br>MoMo callback query parameters. | Adapter tái tạo và so chữ ký HMAC-SHA256; resultCode `0` hoặc `9000` được xem là thành công; tra order từ transaction reference. | HTTP 200; giao dịch được cập nhật; callback không hợp lệ không đánh dấu thành công. |
| **FR-PAY-09 — POST `/api/payments/momo-ipn`**<br>Actor: webhook MoMo; public.<br>JSON payload callback MoMo. | Chuyển các giá trị payload sang chuỗi; dùng cùng logic xác minh chữ ký/kết quả của MoMo. | HTTP 200; response giao dịch. |
| **FR-PAY-10 — POST `/api/payments/vietqr-webhook`**<br>Actor: webhook ngân hàng/VietQR; public.<br>JSON chứa nội dung, amount, transactionReference và secretToken; có thể gửi `X-Webhook-Secret`. | Header secret không rỗng sẽ ghi đè secret trong payload; gateway kiểm tra secret, lấy mã tham chiếu hoặc dò order id từ nội dung chuyển khoản, rồi cập nhật giao dịch/order. | HTTP 200; response giao dịch. Gateway hiện kiểm tra secret và reference nhưng chưa đối chiếu amount callback với tổng tiền order. |
| **FR-PAY-11 — POST `/api/payments/dev-simulate-success`**<br>Actor: route public trong cấu hình hiện tại; thiết kế dùng cho kiểm thử dev.<br>Query `orderId`; `method` mặc định MOMO, hỗ trợ MOMO/VNPAY/CHUYEN_KHOAN_QR. | Tạo payload callback thành công giả lập có chữ ký/secret phù hợp rồi gọi lại xử lý callback. Từ chối order đã thanh toán hoặc hủy. | HTTP 200; giao dịch sau giả lập. Endpoint này được permitAll trong `SecurityConfig`. |

### 3.7. MODULE PROMOTION — Chương trình, giảm giá và voucher (23 endpoint)

Các API promotion không nằm trong danh sách permitAll. Vì vậy GET và validate cũng yêu cầu JWT hợp lệ. CRUD dưới đây rút gọn cùng hành vi trong từng nhóm nhưng mỗi method/path được liệt kê riêng.

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-PROMO-01 — GET `/api/promotion-programs`**<br>Actor: authenticated.<br>`page` mặc định 0, `size` mặc định 20. | Kiểm tra page ≥ 0, size > 0; phân trang chương trình chưa xóa theo createdAt giảm dần. | HTTP 200; `PageResponse` chương trình. |
| **FR-PROMO-02 — GET `/api/promotion-programs/{id}`**<br>Actor: authenticated.<br>`id`. | Tìm chương trình chưa xóa. | HTTP 200; chi tiết chương trình; 404 nếu không thấy. |
| **FR-PROMO-03 — POST `/api/promotion-programs`**<br>Actor: ADMIN, MANAGER.<br>Tên, ngày bắt đầu/kết thúc, cờ hoạt động; mô tả tùy chọn. | Kiểm tra tên không rỗng, ngày đủ và ngày bắt đầu không sau ngày kết thúc, cờ hoạt động có giá trị; lưu campaign. | HTTP 201; chương trình mới. |
| **FR-PROMO-04 — PUT `/api/promotion-programs/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id` và các trường campaign. | Validate như tạo; tìm bản ghi; cập nhật đầy đủ các trường request. | HTTP 200; campaign cập nhật; 404 nếu không thấy. |
| **FR-PROMO-05 — DELETE `/api/promotion-programs/{id}`**<br>Actor: ADMIN.<br>`id`. | Không cho xóa nếu còn khuyến mãi sản phẩm, hóa đơn hoặc voucher liên kết; nếu không, xóa mềm. | HTTP 200; `ApiResponse.success(null)`; xung đột liên kết trả 409. |
| **FR-PROMO-06 — GET `/api/product-promotions`**<br>Actor: authenticated.<br>page/size mặc định 0/20. | Phân trang bản ghi chưa xóa theo thời điểm tạo giảm dần. | HTTP 200; trang cấu hình khuyến mãi sản phẩm. |
| **FR-PROMO-07 — GET `/api/product-promotions/{id}`**<br>Actor: authenticated.<br>`id`. | Tìm rule theo id chưa xóa. | HTTP 200; rule; 404 nếu không có. |
| **FR-PROMO-08 — POST `/api/product-promotions`**<br>Actor: ADMIN, MANAGER.<br>campaign id; product id hoặc category id tối thiểu một; loại giảm và giá trị. | Kiểm tra đủ campaign/đối tượng/loại giảm; giá trị dương; phần trăm không quá 100; xác minh các liên kết tồn tại; tạo rule. | HTTP 201; cấu hình vừa tạo. |
| **FR-PROMO-09 — PUT `/api/product-promotions/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id` và body rule. | Validate như tạo; tìm rule/campaign/product/category; thay nội dung và liên kết. | HTTP 200; rule mới; 404 nếu tham chiếu thiếu. |
| **FR-PROMO-10 — DELETE `/api/product-promotions/{id}`**<br>Actor: ADMIN.<br>`id`. | Đánh dấu rule xóa mềm. | HTTP 200; success null. |
| **FR-PROMO-11 — GET `/api/invoice-promotions`**<br>Actor: authenticated.<br>page/size mặc định 0/20. | Phân trang rule chưa xóa theo createdAt giảm dần. | HTTP 200; trang rule giảm giá hóa đơn. |
| **FR-PROMO-12 — GET `/api/invoice-promotions/{id}`**<br>Actor: authenticated.<br>`id`. | Tìm rule chưa xóa. | HTTP 200; chi tiết rule; 404 nếu không thấy. |
| **FR-PROMO-13 — POST `/api/invoice-promotions`**<br>Actor: ADMIN, MANAGER.<br>campaign, ngưỡng đơn hàng tối thiểu, loại/giá trị giảm, mức giảm tối đa tùy chọn. | Validate campaign tồn tại; min không âm; loại giảm hợp lệ; giá trị dương và % ≤100; mức tối đa nếu có không âm; lưu. | HTTP 201; rule hóa đơn mới. |
| **FR-PROMO-14 — PUT `/api/invoice-promotions/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id` và body rule. | Validate như tạo; tìm rule và campaign; cập nhật rule. | HTTP 200; rule sau cập nhật. |
| **FR-PROMO-15 — DELETE `/api/invoice-promotions/{id}`**<br>Actor: ADMIN.<br>`id`. | Xóa mềm rule hóa đơn. | HTTP 200; success null. |
| **FR-PROMO-16 — GET `/api/vouchers`**<br>Actor: authenticated.<br>page/size mặc định 0/20. | Phân trang voucher chưa xóa theo createdAt giảm dần. | HTTP 200; trang voucher. |
| **FR-PROMO-17 — GET `/api/vouchers/{id}`**<br>Actor: authenticated.<br>`id`. | Tìm voucher chưa xóa. | HTTP 200; voucher; 404 nếu không thấy. |
| **FR-PROMO-18 — POST `/api/vouchers`**<br>Actor: ADMIN, MANAGER.<br>campaign id, code, loại/giá trị giảm, min, max tùy chọn, giới hạn lượt tùy chọn, số lượt đã dùng tùy chọn. | Validate các giá trị; chuẩn hóa code uppercase/trim; từ chối code trùng; xác minh campaign; mặc định số lượt đã dùng 0. | HTTP 201; voucher. Trùng code trả 409. |
| **FR-PROMO-19 — PUT `/api/vouchers/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id` và body voucher. | Validate; chuẩn hóa và kiểm tra code trùng; xác minh campaign; cập nhật các trường. | HTTP 200; voucher cập nhật. |
| **FR-PROMO-20 — DELETE `/api/vouchers/{id}`**<br>Actor: ADMIN.<br>`id`. | Đánh dấu voucher xóa mềm. | HTTP 200; success null. |
| **FR-PROMO-21 — POST `/api/vouchers/validate`**<br>Actor: mọi role đã đăng nhập (không public theo `SecurityConfig`).<br>`maCode`, `tongTienDonHang >= 0`. | Chuẩn hóa code; kiểm tra tồn tại, campaign hoạt động/chưa xóa/đúng thời gian, quota và giá trị tối thiểu; tính giảm cố định hoặc phần trăm (làm tròn 2 số, áp cap dương); giới hạn giảm trong 0..tổng đơn. | HTTP 200; code, `hopLe`, tiền giảm, tiền sau giảm và thông điệp lý do. Không hợp lệ trả kết quả `hopLe=false`. |
| **FR-PROMO-22 — POST `/api/vouchers/apply`**<br>Actor: ADMIN, MANAGER, CASHIER, CUSTOMER.<br>Cùng body validate. | Validate trước; nếu hợp lệ thì tăng lượt đã dùng bằng câu lệnh atomic có điều kiện quota; từ chối nếu quota vừa hết. | HTTP 200; kết quả validation. Lỗi không hợp lệ/quota hết trả 400. |
| **FR-PROMO-23 — POST `/api/vouchers/release/{maCode}`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>Code trên path. | Chuẩn hóa code, tìm voucher và giảm bộ đếm đã dùng bằng lệnh atomic. Controller/service không gắn release với một order cụ thể. | HTTP 200; success null; 404 nếu code không có. |

### 3.8. MODULE SHIFT — Ca làm việc (8 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-SHIFT-01 — GET `/api/shifts`**<br>Actor: ADMIN, MANAGER.<br>`status` tùy chọn. | Lấy ca chưa xóa, có thể lọc trạng thái; mỗi response được bổ sung doanh thu đơn, số đơn, tiền mặt dự kiến và chênh lệch nếu đã chốt. | HTTP 200; danh sách ca (không phân trang). Repository đang cộng `tong_tien_thanh_toan` của mọi order theo ca, không lọc phương thức thanh toán hay trạng thái order. |
| **FR-SHIFT-02 — POST `/api/shifts`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>Body CaLamViecRequest; thu ngân, giờ, tiền đầu/kết ca, trạng thái là các trường DTO. | Controller gọi logic mở ca giống `/open`; nếu thiếu mã thu ngân thì dùng id từ principal; service xác nhận user hoạt động, chưa có ca mở, tiền đầu ca không âm; luôn tạo `DANG_MO`. | HTTP 201; ca mới kèm doanh thu/tổng hợp. |
| **FR-SHIFT-03 — POST `/api/shifts/open`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>Request tương tự POST `/api/shifts`; nếu thiếu `maThuNgan`, controller lấy id từ JWT principal. | Dùng cùng validation và tạo ca `DANG_MO`; mặc định giờ bắt đầu hiện tại, tiền đầu ca 0 nếu không truyền, tiền kết ca 0. | HTTP 201; ca vừa mở. Một cashier chỉ có một ca `DANG_MO` theo kiểm tra hiện tại. |
| **FR-SHIFT-04 — GET `/api/shifts/current`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>JWT principal tùy trường hợp. | Tìm ca DANG_MO theo user id; service fallback sang ca DANG_MO gần nhất bất kỳ nếu không thấy ca của user. | HTTP 200; ca hoặc null. |
| **FR-SHIFT-05 — GET `/api/shifts/{id}`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`id`. | Tải ca và dựng các trường báo cáo ca. Không kiểm tra quyền sở hữu của CASHIER trong service. | HTTP 200; chi tiết ca; 404 nếu không thấy. |
| **FR-SHIFT-06 — PUT `/api/shifts/{id}`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`tienDauCa` hoặc `tienKetCa`, thời gian/trạng thái tùy chọn. | Nếu request yêu cầu DA_DONG hoặc có tiền kết ca, kiểm tra ca chưa đóng và tiền kết ca có/không âm, đặt thời điểm kết thúc và trạng thái DA_DONG. Nếu vẫn mở, chỉ cho cập nhật tiền đầu ca không âm. | HTTP 200; ca và dữ liệu tổng hợp. |
| **FR-SHIFT-07 — POST `/api/shifts/{id}/close`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`tienKetCa` và các trường CaLamViecRequest. | Gọi cùng service update như PUT; chốt ca nếu có tiền kết ca hoặc trạng thái DA_DONG. Chênh lệch = tiền kết ca − (tiền đầu ca + doanh thu tổng hợp). | HTTP 200; ca đã đóng và các số liệu đối soát. |
| **FR-SHIFT-08 — DELETE `/api/shifts/{id}`**<br>Actor: ADMIN.<br>`id`. | Tải ca và đánh dấu xóa mềm; không kiểm tra trạng thái đã đóng trước khi xóa. | HTTP 200; success null. |

### 3.9. MODULE TABLE — Bàn và QR (7 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-TABLE-01 — GET `/api/tables`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>Không có input. | Lấy bàn chưa xóa, sắp xếp số bàn tăng dần. | HTTP 200; danh sách bàn và trạng thái. |
| **FR-TABLE-02 — POST `/api/tables`**<br>Actor: ADMIN, MANAGER.<br>`soBan` bắt buộc. | Trim và kiểm tra số bàn không trùng; tự sinh UUID QR; tạo trạng thái TRONG. | HTTP 201; bàn mới kèm QR token. Trùng số bàn trả 409. |
| **FR-TABLE-03 — GET `/api/tables/{id}`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`id`. | Tải bàn chưa xóa theo id. | HTTP 200; bàn; 404 nếu không thấy. |
| **FR-TABLE-04 — PUT `/api/tables/{id}`**<br>Actor: ADMIN, MANAGER.<br>`soBan`, `trangThai`, `maQrToken` tùy chọn. | Kiểm tra bàn; cập nhật số bàn nếu có và không trùng; kiểm tra chuyển trạng thái nếu có; khi `maQrToken` được gửi thì tạo UUID mới (không dùng giá trị token gửi lên). | HTTP 200; bàn sau cập nhật. |
| **FR-TABLE-05 — PATCH `/api/tables/{id}/status`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`trangThai` trong BanRequest. | Kiểm tra trạng thái đích thuộc TRONG/DANG_CO_KHACH/DA_DAT_TRUOC và transition: DANG_CO_KHACH chỉ về TRONG; DA_DAT_TRUOC về DANG_CO_KHACH hoặc TRONG. | HTTP 200; bàn sau chuyển trạng thái; dữ liệu không hợp lệ trả 400. |
| **FR-TABLE-06 — DELETE `/api/tables/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`. | Chỉ cho xóa mềm khi trạng thái TRONG; bàn đang sử dụng trả 409. | HTTP 200; success null. |
| **FR-TABLE-07 — GET `/api/tables/qr/{token}`**<br>Actor: public/GUEST.<br>QR token trên path. | Trim và tìm bàn chưa xóa theo token. Endpoint chỉ trả thông tin bàn, chưa tự tạo session đặt món. | HTTP 200; bàn, số bàn, token, trạng thái; 400 token rỗng hoặc 404 không tồn tại. |

### 3.10. MODULE CUSTOMER — Hồ sơ khách hàng (5 endpoint)

Các endpoint này là API quản trị/tìm hồ sơ, không phải API self-service của khách. JWT role CUSTOMER hiện không được phép gọi chúng.

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-CUST-01 — GET `/api/customers`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`phone` tùy chọn. | Nếu có phone thì tìm khớp chính xác sau trim; nếu không có thì lấy tất cả hồ sơ chưa xóa. | HTTP 200; danh sách khách (không phân trang). |
| **FR-CUST-02 — GET `/api/customers/{id}`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>`id`. | Tìm hồ sơ chưa xóa theo id. | HTTP 200; hồ sơ gồm tên, điện thoại, email, điểm/hạng và trạng thái theo response DTO; 404 nếu không thấy. |
| **FR-CUST-03 — POST `/api/customers`**<br>Actor: ADMIN, MANAGER, CASHIER.<br>SĐT VN hợp lệ (`0` hoặc `+84` kèm đầu số di động), tên bắt buộc ≤100, email hợp lệ tùy chọn, mật khẩu tùy chọn tối thiểu 6 ký tự. | Từ chối số điện thoại đã dùng; map thông tin tạo; BCrypt mật khẩu nếu gửi; lưu hồ sơ khách. | HTTP 200 (controller không đặt 201); hồ sơ khách mới; số điện thoại trùng trả 409. |
| **FR-CUST-04 — PUT `/api/customers/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`, tên bắt buộc ≤100 và email hợp lệ tùy chọn. | Tìm khách, map tên/email cập nhật và lưu; request không có trường SĐT nên không đổi số điện thoại. | HTTP 200; hồ sơ sau cập nhật; 404 nếu không thấy. |
| **FR-CUST-05 — DELETE `/api/customers/{id}`**<br>Actor: ADMIN.<br>`id`. | Đánh dấu hồ sơ deleted_at. | HTTP 200; `ApiResponse.success(null)`. |

### 3.11. MODULE INVENTORY — Nhà cung cấp và phiếu nhập (17 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-INV-01 — GET `/api/suppliers`**<br>Actor: mọi role đã đăng nhập.<br>`page` mặc định 0, `size` 20. | Lấy nhà cung cấp chưa xóa, sắp xếp id tăng dần và phân trang. | HTTP 200; `PageResponse<NhaCungCapResponse>`. |
| **FR-INV-02 — GET `/api/suppliers/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id`. | Tìm nhà cung cấp chưa xóa. | HTTP 200; chi tiết; 404 nếu không thấy. |
| **FR-INV-03 — POST `/api/suppliers`**<br>Actor: ADMIN, MANAGER.<br>Tên và SĐT bắt buộc; email, địa chỉ, MST, đại diện, đang hợp tác, ngày tạo tùy chọn. | Kiểm tra tên/SĐT; chuẩn hóa chuỗi; từ chối SĐT trùng; mặc định đang hợp tác true và thời gian hiện tại nếu thiếu. | HTTP 201; nhà cung cấp mới; trùng SĐT trả 409. |
| **FR-INV-04 — PUT `/api/suppliers/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id` và các trường nhà cung cấp tùy chọn. | Tải nhà cung cấp; chỉ cập nhật trường được gửi; kiểm tra SĐT mới không trùng. | HTTP 200; nhà cung cấp sau cập nhật. |
| **FR-INV-05 — DELETE `/api/suppliers/{id}`**<br>Actor: ADMIN.<br>`id`. | Không cho xóa mềm nếu có phiếu nhập liên kết; nếu chưa liên kết thì đánh dấu deleted_at. | HTTP 200; success null; xung đột trả 409. |
| **FR-INV-06 — GET `/api/purchase-orders`**<br>Actor: mọi role đã đăng nhập.<br>Filter `supplierId`, `fromDate`/`toDate` hoặc alias `from`/`to`, `trangThai`; page/size mặc định 0/20. | Alias `from`/`to` ưu tiên hơn fromDate/toDate; kiểm tra ngày đầu không sau ngày cuối; tìm theo filter và sắp ngày nhập mới nhất trước. | HTTP 200; trang phiếu nhập. Khoảng ngày sai trả 400. |
| **FR-INV-07 — GET `/api/purchase-orders/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id`. | Tải phiếu nhập chưa xóa mềm. | HTTP 200; phiếu và chi tiết được mapper trả về; 404 nếu không thấy. |
| **FR-INV-08 — POST `/api/purchase-orders`**<br>Actor: ADMIN, MANAGER.<br>`maNhaCungCap`; `maNguoiNhap` tùy chọn (controller lấy user id từ principal nếu thiếu); mã phiếu tùy chọn; ghi chú, ngày nhập, danh sách dòng tùy chọn. | Xác minh supplier đang hợp tác và người nhập đang hoạt động; giữ mã phiếu request nếu unique, nếu không sinh `PN-{8 ký tự UUID}`; luôn tạo trạng thái CHO_DUYET; validate từng dòng, liên kết item và tính thành tiền/tổng từ số lượng × đơn giá. `tongTien` và `trangThai` trong request không quyết định giá trị lưu. | HTTP 201; phiếu nhập kèm tổng và chi tiết. Phiếu rỗng có thể tạo nhưng không thể duyệt. |
| **FR-INV-09 — PUT `/api/purchase-orders/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`; supplier, người nhập, ghi chú tùy chọn. | Chỉ sửa phiếu CHO_DUYET; nếu đổi supplier/người nhập thì xác minh liên kết còn hợp lệ; cập nhật ghi chú. Không cập nhật chi tiết ở route này. | HTTP 200; phiếu sau cập nhật; trạng thái khác CHO_DUYET trả 400. |
| **FR-INV-10 — POST `/api/purchase-orders/{id}/approve`**<br>Actor: ADMIN, MANAGER.<br>`id`. | Chỉ duyệt phiếu CHO_DUYET và có ít nhất một dòng; cộng số lượng vào tồn của variant/topping; cập nhật cờ còn hàng; chuyển DA_NHAP_KHO. Duyệt lại xung đột; phiếu hủy/khác trạng thái bị từ chối. | HTTP 200; phiếu đã duyệt. Route POST và PATCH gọi cùng handler, thao tác có kiểm tra trạng thái trước khi cộng kho. |
| **FR-INV-11 — PATCH `/api/purchase-orders/{id}/approve`**<br>Actor: ADMIN, MANAGER.<br>`id`. | Alias method của FR-INV-10; xử lý cùng validation/trạng thái và tăng tồn. | HTTP 200; cùng response như POST approve. |
| **FR-INV-12 — DELETE `/api/purchase-orders/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`. | Chỉ hủy phiếu CHO_DUYET; đặt DA_HUY và đánh dấu xóa mềm cả phiếu lẫn các dòng hiện hành. | HTTP 200; success null; phiếu đã duyệt không hủy được qua route này. |
| **FR-INV-13 — GET `/api/purchase-order-lines`**<br>Actor: mọi role đã đăng nhập.<br>Bắt buộc `maPhieuNhap`; page/size mặc định 0/20. | Truy vấn các dòng chưa xóa theo id phiếu, phân trang và sắp id tăng dần. | HTTP 200; `PageResponse` dòng phiếu nhập. |
| **FR-INV-14 — GET `/api/purchase-order-lines/{id}`**<br>Actor: mọi role đã đăng nhập.<br>`id`. | Tải dòng chưa xóa theo id. | HTTP 200; chi tiết dòng; 404 nếu không thấy. |
| **FR-INV-15 — POST `/api/purchase-order-lines`**<br>Actor: ADMIN, MANAGER.<br>`maPhieuNhap`, variant id hoặc topping id, quantity >0, đơn giá nhập ≥0; tên/đơn vị tùy chọn. | Kiểm tra phiếu CHO_DUYET và item tham chiếu; cần ít nhất một trong variant/topping; suy ra tên và đơn vị mặc định nếu thiếu; tính thành tiền; lưu dòng và tính lại tổng phiếu. | HTTP 201; dòng vừa thêm; validation lỗi trả 400/404. |
| **FR-INV-16 — PUT `/api/purchase-order-lines/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`; số lượng, đơn giá, tên hàng, đơn vị tùy chọn. | Chỉ sửa nếu phiếu cha CHO_DUYET; kiểm tra quantity dương/đơn giá không âm; cập nhật trường được gửi; tính lại thành tiền và tổng phiếu. Các trường tham chiếu từ body không dùng để chuyển item. | HTTP 200; dòng sau cập nhật; phiếu khác trạng thái trả 400. |
| **FR-INV-17 — DELETE `/api/purchase-order-lines/{id}`**<br>Actor: ADMIN, MANAGER.<br>`id`. | Chỉ xóa mềm dòng khi phiếu cha CHO_DUYET; tính lại tổng phiếu. | HTTP 200; success null. |

### 3.12. MODULE REPORT — Báo cáo (1 endpoint)

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **FR-RPT-01 — GET `/api/reports`**<br>Actor: mọi role đã đăng nhập theo fallback security.<br>Không có query parameter. | Controller chưa gọi `ReportService`; không có truy vấn/filter/KPI thực thi. Service hiện ném UnsupportedOperationException nếu được gọi trực tiếp. | HTTP 200 với `ApiResponse.success(null)`; chưa có báo cáo doanh thu/top sản phẩm để sử dụng. |

### 3.13. Endpoint phụ trợ ngoài 12 module

| INPUT — chức năng, actor và dữ liệu | PROCESSING | OUTPUT |
|:---|:---|:---|
| **GET `/api/health`**<br>Actor: public.<br>Không có input. | Trả trạng thái cố định UP. | HTTP 200; `{status: "UP"}` trong ApiResponse. Endpoint này không tính trong 107 endpoint nghiệp vụ. |

---

## 4. MA TRẬN PHÂN QUYỀN

| Chức năng / module | GUEST | CUSTOMER | CASHIER | BARISTA | MANAGER | ADMIN |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Auth — login** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Auth — register CUSTOMER | ✅ | — | — | — | — | — |
| Auth — refresh/logout với token hợp lệ | ✅* | ✅ | ✅ | ✅ | ✅ | ✅ |
| Auth — `/me` | — | ✅ | ✅ | ✅ | ✅ | ✅ |
| **User — CRUD nhân viên/vai trò** | — | — | — | — | — | — |
| **Catalog — đọc category/product/variant/topping** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Catalog — tạo/sửa category, product, variant, topping | — | — | — | — | ✅ | ✅ |
| Catalog — xóa category/product/topping | — | — | — | — | — | ✅ |
| Catalog — xóa variant | — | — | — | — | — | — |
| **Cart — add/read/update/remove/clear** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Order — tạo đơn từ cart** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Order — tạo đơn POS | — | — | ✅ | — | ✅ | ✅ |
| Order — xem lịch sử cá nhân | — | ✅ | — | — | — | — |
| Order — list tất cả đơn | — | — | ✅ | — | ✅ | ✅ |
| Order — xem chi tiết | — | ✅‡ | ✅ | ✅ | ✅ | ✅ |
| Order — cập nhật trạng thái | — | — | ✅ | ✅ | ✅ | ✅ |
| Order — hủy đơn | — | ✅‡ | — | — | ✅ | ✅ |
| **Payment — list/get/create/update/delete** | — | ✅ | ✅ | ✅ | ✅ | ✅ |
| Payment — VNPay/MoMo/VietQR callback | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Payment — dev simulate success | ✅* | ✅* | ✅* | ✅* | ✅* | ✅* |
| **Promotion — list/detail/validate voucher** | — | ✅ | ✅ | ✅ | ✅ | ✅ |
| Promotion — create/update campaign/rule/voucher | — | — | — | — | ✅ | ✅ |
| Promotion — xóa campaign/rule/voucher | — | — | — | — | — | ✅ |
| Promotion — apply voucher | — | ✅ | ✅ | — | ✅ | ✅ |
| Promotion — release voucher | — | — | ✅ | — | ✅ | ✅ |
| **Shift — list toàn bộ ca** | — | — | — | — | ✅ | ✅ |
| Shift — open/current/detail/update/close | — | — | ✅ | — | ✅ | ✅ |
| Shift — delete | — | — | — | — | — | ✅ |
| **Table — list/detail** | — | — | ✅ | — | ✅ | ✅ |
| Table — tạo/sửa/xóa bàn | — | — | — | — | ✅ | ✅ |
| Table — đổi trạng thái | — | — | ✅ | — | ✅ | ✅ |
| Table — tra cứu QR | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Customer — list/get/create hồ sơ** | — | — | ✅ | — | ✅ | ✅ |
| Customer — cập nhật hồ sơ | — | — | — | — | ✅ | ✅ |
| Customer — xóa hồ sơ | — | — | — | — | — | ✅ |
| **Inventory — list/get nhà cung cấp, phiếu và dòng** | — | ✅ | ✅ | ✅ | ✅ | ✅ |
| Inventory — CRUD supplier (tạo/sửa) | — | — | — | — | ✅ | ✅ |
| Inventory — xóa supplier | — | — | — | — | — | ✅ |
| Inventory — tạo/sửa/duyệt/hủy phiếu và dòng | — | — | — | — | ✅ | ✅ |
| **Report — GET summary hiện tại** | — | ✅ | ✅ | ✅ | ✅ | ✅ |

`✅` quyền thực tế theo whitelist, `@PreAuthorize` hoặc kiểm tra service; `—` không có quyền tương ứng.

- `*` Route public; refresh cần refresh token hợp lệ. Logout không cần access JWT nhưng chỉ thu hồi token nếu body có refresh token. Dev simulate hiện cũng public theo cấu hình.
- `/api/carts/**` public; chỉ CUSTOMER được liên kết với hồ sơ khách trong service, các role khác dùng guest-cart semantics. `POST /api/orders` cũng public và có thể tạo đơn bằng cartToken; nếu gửi customerId thì service giới hạn theo role/owner.
- `‡` CUSTOMER chỉ được xem/hủy order của chính mình; những người chưa đăng nhập không xem chi tiết được.
- Các endpoint đọc không public ở Payment/Promotion/Shift/Table/Customer/Inventory/Report vẫn yêu cầu JWT theo quy tắc `.anyRequest().authenticated()` dù controller không có `@PreAuthorize`.
- `@SecurityRequirement` trong OpenAPI chỉ mô tả tài liệu Swagger, không tự cấp quyền hay làm endpoint public.

---

## 5. BUSINESS RULES TOÀN HỆ THỐNG

### 5.1. Tài khoản, xác thực và token

- `username` unique; password lưu dạng BCrypt. Tài khoản inactive không đăng nhập được.
- Access JWT sống 15 phút, refresh JWT 7 ngày theo `application.yml`; thời lượng được cấu hình qua biến ứng dụng.
- Refresh token được ghi Redis, mỗi lần refresh kiểm tra và xóa token cũ rồi cấp token mới. Logout không blacklist access token.
- CUSTOMER đăng ký qua bảng `nguoi_dung` với vai trò CUSTOMER. Cart/Order có thể tạo hồ sơ `khach_hang` liên quan theo SĐT khi cần.

### 5.2. Cart và giá

- Guest cart dùng UUID `X-Cart-Token`; khi CUSTOMER thực hiện request cart, service chuyển các dòng của guest cart hợp lệ sang cart khách.
- Tiền trong cart được tính từ giá variant/topping hiện tại ở server; request client không quyết định giá.
- Khi tạo order, giá variant/topping, số lượng, tùy chọn đường/đá và ghi chú được copy vào chi tiết đơn. Giá snapshot không phụ thuộc các lần cập nhật catalog sau đó.

### 5.3. State machine đơn hàng và tồn kho

```text
CHO_XAC_NHAN ──> DA_THANH_TOAN ──> DANG_PHA_CHE ──> SAN_SANG ──> HOAN_THANH
      └──────────────────────────────┴──────────────────────────────> DA_HUY
```

- Các cạnh hợp lệ chính xác: `CHO_XAC_NHAN → DA_THANH_TOAN|DA_HUY`; `DA_THANH_TOAN → DANG_PHA_CHE|DA_HUY`; `DANG_PHA_CHE → SAN_SANG|DA_HUY`; `SAN_SANG → HOAN_THANH|DA_HUY`. HOAN_THANH và DA_HUY là kết thúc.
- Tồn variant/topping chỉ bị trừ khi order chuyển DA_THANH_TOAN; chỉ hoàn lại khi chuyển DA_HUY sau khi đã trừ. Cờ `don_hang.ton_kho_da_tru` chặn hoàn/trừ lặp qua luồng này.
- `BienTheSanPham` và `Topping` có `@Version`; optimistic locking giúp phát hiện cập nhật đồng thời. Mã hóa conflict tại HTTP cần xem cách lỗi Hibernate được xử lý ở tầng exception.
- Không có rule tự động tích điểm khi hoàn thành order trong `OrderServiceImpl`/Customer service hiện tại.

### 5.4. Mã đơn hàng

- Mã hiển thị: `DH-{năm}-{số thứ tự tối thiểu 3 chữ số}` (ví dụ `DH-2026-001`); năm tính theo `Asia/Ho_Chi_Minh`.
- `order_sequence` lưu counter riêng từng năm; câu lệnh `INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING` tăng counter atomic để tránh trùng số đồng thời.

### 5.5. Thanh toán

- Adapter hiện đăng ký: `TIEN_MAT`, `MOMO`, `VNPAY`, `CHUYEN_KHOAN_QR`. `THE_NGAN_HANG` có trong tài liệu enum cũ nhưng chưa có gateway strategy, nên factory từ chối.
- Khởi tạo thanh toán chỉ nhận order CHO_XAC_NHAN và yêu cầu số tiền khớp 100% tổng thanh toán. Cash thành công ngay; online chờ callback.
- Callback VNPay/MoMo kiểm tra chữ ký HMAC; VietQR kiểm tra webhook secret. Callback lặp không cập nhật order đã DA_THANH_TOAN lần nữa.
- Callback handler hiện không đối chiếu amount callback với `tong_tien_thanh_toan` của order. Riêng VietQR adapter xác minh shared secret và mã tham chiếu, nhưng không xác thực amount; cần coi đây là giới hạn implementation hiện hành.
- Giao dịch THANH_CONG không được xóa; update giao dịch trực tiếp không hỗ trợ. Chưa có luồng hoàn tiền triển khai.

### 5.6. Khuyến mãi và voucher

- Loại giảm: `PHAN_TRAM`, `TIEN_CO_DINH`; phần trăm không vượt 100. Validate voucher xét campaign hoạt động/thời gian, quota và ngưỡng đơn; giảm không vượt tổng tiền đơn.
- Apply voucher tăng `so_luot_da_dung` bằng truy vấn atomic có quota guard; release giảm counter nhưng không nhận order id nên chưa bảo đảm liên kết một lần release cho từng đơn.
- CRUD khuyến mãi và validate/apply voucher đã có. Tuy nhiên tạo order chưa gọi promotion module, chưa lưu mức giảm/voucher vào snapshot order; `applyTotals` hiện đặt giảm giá bằng 0. Không có thứ tự tự động áp campaign sản phẩm → hóa đơn → voucher trong order flow.

### 5.7. Ca làm việc và bàn

- Tạo ca luôn đặt `DANG_MO`; kiểm tra mỗi user có tối đa một ca đang mở; tiền đầu ca không âm. Đóng ca cần tiền kết ca không âm và chỉ đóng một lần.
- Số liệu ca hiện cộng tổng tiền của mọi đơn gắn ca, không lọc `TIEN_MAT`, trạng thái order hoặc phương thức giao dịch. Chênh lệch được tính trên số tổng này, vì vậy chưa thể xem là đối soát tiền mặt thuần.
- Trạng thái bàn hợp lệ là `TRONG`, `DANG_CO_KHACH`, `DA_DAT_TRUOC`; khi DANG_CO_KHACH chỉ có thể về TRONG; khi DA_DAT_TRUOC chỉ chuyển DANG_CO_KHACH hoặc TRONG.
- Tạo bàn tự sinh QR UUID; tra QR chỉ trả dữ liệu bàn, không khởi tạo order/session tại bàn.

### 5.8. Nhập kho và dữ liệu lưu trữ

- Phiếu nhập mới phải được service gán `CHO_DUYET` tường minh (migration V1 có default `DA_NHAP_KHO`). Chỉ phiếu chờ mới sửa/hủy/duyệt; phiếu duyệt cần có ít nhất một dòng; duyệt cộng tồn rồi chuyển `DA_NHAP_KHO`.
- Dòng nhập kiểm tra quantity >0 và đơn giá không âm; tổng dòng/tổng phiếu được tính từ server. Service yêu cầu có variant hoặc topping nhưng hiện không từ chối khi gửi đồng thời cả hai; khi duyệt nhánh variant được ưu tiên, nên không nên gửi cả hai id.
- Hầu hết entity nghiệp vụ kế thừa audit `created_at`, `updated_at`, `deleted_at` và dùng soft delete; counter `order_sequence` không phải entity audit. Ràng buộc DB nổi bật gồm unique username/phone/table/QR/order code/voucher code và unique size trong một sản phẩm.
- Redis hiện được dùng rõ ràng cho refresh token; không thấy cache menu/report trong service được quét.

### 5.9. Dữ liệu khách hàng và nội dung chưa được triển khai

- Bảng khách có điểm/hạng, nhưng hiện không có API self-service profile, sổ địa chỉ, lịch sử điểm hoặc logic cộng điểm/nâng hạng. Không có bảng địa chỉ trong migration.
- Không có User CRUD API, báo cáo nghiệp vụ, upload ảnh sản phẩm, API tra cứu đơn guest, API quên mật khẩu, hoặc POS/KDS UI trong phạm vi frontend hiện tại.
- Không thấy cơ chế cache Redis cho catalog/report, cũng không thấy tầng role permission granularity ngoài role và các `@PreAuthorize` hiện có.

---

## 6. PHẠM VI GIAO DIỆN VÀ TRẠNG THÁI TRIỂN KHAI

### 6.1. Customer Web

Frontend khai báo các route `/`, `/menu`, `/menu/:productId`, `/cart`, `/checkout`, `/checkout/payment`, callback thanh toán, tạo đơn thành công, track order, QR tại bàn, profile, order history/tracking, loyalty, addresses và favorites. Các route Customer không tương đương toàn bộ API backend: chẳng hạn backend chưa có API địa chỉ/favorites/points hoặc tra cứu order guest. `/qr/:tableToken` hiện dùng `PlaceholderPage`; `track-order-page.tsx` còn TODO gọi `/orders/track` (backend không có endpoint này).

### 6.2. Admin Dashboard

Route quản trị khai báo dashboard, products, categories, toppings, orders, promotions, vouchers, customers, employees, shifts, tables, inventory, suppliers, reports và settings. Admin/Manager được bảo vệ ở layout; employees giới hạn thêm ADMIN.

Các trang frontend gọi thẳng placeholder trong source gồm: customer detail/customers, employees, inventory, promotions, reports, settings, shifts, suppliers. Do đó route có tồn tại không đồng nghĩa trang/module đã dùng được. Backend cũng không có User API, report summary hiện rỗng và một số nghiệp vụ self-service chưa có API.

### 6.3. Bảng trạng thái theo source hiện tại

| Module | Backend theo implementation | Frontend trong repository |
|:---|:---|:---|
| auth | Login/register/refresh/logout/me có code | Login/register có page; forgot-password là route FE nhưng chưa có API backend |
| user | Entity/repository và role seed; chưa có quản trị REST | `/admin/employees` là placeholder |
| catalog | CRUD/danh sách sản phẩm, category, variant, topping | Các route/page sản phẩm, danh mục, topping có trong app |
| cart | CRUD cart item và tính tiền server | Cart/checkout pages có trong app |
| order | Tạo order online/POS, query, status/cancel | Customer/admin order routes có trong app; POS/KDS ngoài phạm vi |
| payment | Tiền mặt, adapter online, callback; update direct không hỗ trợ | Payment/callback pages có trong app; mức tích hợp end-to-end phụ thuộc cấu hình gateway |
| promotion | CRUD campaign/rules/voucher, validate/apply/release; chưa tích hợp vào tạo order | Promotions page placeholder; vouchers route/page riêng |
| shift | CRUD/lifecycle ca và summary hiện có | Shifts page placeholder |
| table | CRUD trạng thái và public QR lookup | Tables route/page có; QR ordering page placeholder |
| customer | CRUD hồ sơ nội bộ; không có self-service/loyalty API | Admin customers/detail placeholder; Customer profile routes có nhưng chưa có API tương ứng đầy đủ |
| inventory | Supplier, phiếu nhập, dòng, duyệt cộng kho có code | Inventory và suppliers page placeholder |
| report | `/api/reports` trả null, service TODO | Reports page placeholder |

**Lưu ý phiên bản:** prompt đầu vào nêu React 18; `D:\koithe-fe\package.json` trong repository khai báo React 19.2.x và React Router 7.18.x.

---

## 7. TỔNG KẾT VÀ CĂN CỨ SOURCE CODE

### Thống kê

| Hạng mục | Kết quả |
|:---|:---:|
| Module nghiệp vụ | 12 |
| Endpoint trong 12 module | 107 method + path |
| Dòng IPO endpoint | 107 |
| Role DB seed | 5; cộng GUEST anonymous actor thành 6 actor thường dùng |
| Bảng database | 24 (23 ở V1, thêm `order_sequence` ở V5) |
| Endpoint module User | 0 |

### Bao gồm

- Tổng quan kiến trúc và actor theo role thật được seed.
- Danh sách endpoint theo module kèm số lượng, actor/quyền và IPO.
- Ma trận phân quyền đối chiếu `SecurityConfig`, `@PreAuthorize` và service.
- Business rules về token, giỏ, snapshot giá, state machine, tồn kho, thanh toán, voucher, ca, bàn, nhập kho và audit.
- Trạng thái UI/placeholder đối chiếu router và page components frontend.

### File đã tạo

- `docs/functions-specification.md`

### Căn cứ đã quét

- Backend controllers, service implementations, DTOs, entities, `SecurityConfig`, `JwtService` và repository liên quan.
- Flyway migrations `V1__init_schema.sql` đến `V6__seed_test_data.sql` (V6 là seed dữ liệu thử, không thêm bảng).
- Frontend `src/router/index.tsx`, `src/router/lazy-pages.tsx`, các page có `PlaceholderPage`, `src/pages/customer/track-order-page.tsx`, `package.json`.

### Bước tiếp theo phù hợp cho báo cáo

- Bổ sung use case/activity diagram sau khi thống nhất các nghiệp vụ còn thiếu (tích hợp promotion vào order, điểm thành viên, đối soát tiền mặt, report).
- Đối chiếu các trang placeholder với kế hoạch phân công frontend trước khi ghi trạng thái “hoàn thành”.

---

**HẾT TÀI LIỆU**
