# Hướng dẫn theo dõi database bằng VS Code

Tài liệu này giúp partner kết nối và xem dữ liệu PostgreSQL của MilkTea API trong VS Code. Mục tiêu là quan sát, truy vấn và hỗ trợ debug; không dùng để sửa trực tiếp dữ liệu production khi chưa có backup và quy trình được phê duyệt.

## 1. Cài extension

Trong VS Code, mở **Extensions** (`Ctrl+Shift+X`) và cài một PostgreSQL database client. Có thể dùng **Database Client JDBC**; extension này cung cấp giao diện tạo connection, duyệt schema và chạy SQL.

Sau khi cài, khởi động lại VS Code nếu extension yêu cầu.

## 2. Thông tin kết nối local

Chạy riêng PostgreSQL qua Docker Compose trước:

```powershell
docker compose up -d postgres
docker compose ps
```

Tạo connection PostgreSQL trong extension với các giá trị dưới đây:

| Trường | Giá trị local mặc định |
| --- | --- |
| Host | `localhost` |
| Port | `5433` |
| Database | `milktea` |
| Username | `milktea` |
| Password | Giá trị `POSTGRES_PASSWORD` trong `.env` |
| SSL | Tắt cho local Docker mặc định |

Các giá trị trên lấy từ `.env` và `docker-compose.yml`. Nếu partner đổi `POSTGRES_PORT`, `POSTGRES_DB` hoặc `POSTGRES_USER` trong `.env`, phải dùng giá trị đã đổi đó.

> Không dùng host `postgres` trong VS Code trên máy local. `postgres` chỉ là hostname nội bộ giữa các container Docker; VS Code trên máy host phải dùng `localhost`.

## 3. Tạo connection trong VS Code

Quy trình có thể khác đôi chút giữa các extension, nhưng thường là:

1. Mở panel của database extension ở thanh bên trái.
2. Chọn **Add Connection** hoặc **Create Connection**.
3. Chọn PostgreSQL.
4. Nhập các giá trị ở bảng kết nối local.
5. Chọn **Test Connection** trước khi lưu.
6. Lưu connection với tên rõ ràng, ví dụ `MilkTea Local`.

Khi kết nối thành công, mở database `milktea` để duyệt `Schemas` → `public` → `Tables`.

## 4. Các bảng nên kiểm tra

| Mục đích | Bảng |
| --- | --- |
| Xác nhận Flyway đã chạy | `flyway_schema_history` |
| Tài khoản và phân quyền | `nguoi_dung`, `vai_tro` |
| Danh mục, món, biến thể, tồn kho | `loai_san_pham`, `san_pham`, `bien_the_san_pham`, `topping` |
| Giỏ hàng | `gio_hang`, `chi_tiet_gio_hang`, `topping_gio_hang` |
| Đơn hàng, trạng thái, thanh toán | `don_hang`, `chi_tiet_don_hang`, `topping_don_hang`, `giao_dich_thanh_toan` |

Các migration nằm trong `src/main/resources/db/migration/`; schema được Flyway kiểm soát. Không tạo hoặc sửa bảng bằng giao diện VS Code trừ khi có migration đi kèm trong source code.

## 5. Truy vấn kiểm tra an toàn

Mở SQL editor từ connection `MilkTea Local`, sau đó chạy các truy vấn chỉ đọc dưới đây.

### Xem lịch sử migration

```sql
SELECT installed_rank, version, description, type, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Mọi migration cần có `success = true`.

### Kiểm tra role và người dùng

```sql
SELECT
  nd.id,
  nd.ten_dang_nhap,
  nd.ho_va_ten,
  vt.ten_vai_tro,
  nd.dang_hoat_dong,
  nd.deleted_at
FROM nguoi_dung nd
JOIN vai_tro vt ON vt.id = nd.ma_vai_tro
ORDER BY nd.id;
```

Không chia sẻ giá trị `mat_khau_ma_hoa`; đó là hash mật khẩu.

### Xem menu và tồn kho biến thể

```sql
SELECT
  sp.id AS product_id,
  lsp.ten_danh_muc,
  sp.ten_san_pham,
  bt.id AS variant_id,
  bt.kich_co,
  bt.gia_ban,
  bt.so_luong_ton,
  bt.con_hang
FROM san_pham sp
JOIN loai_san_pham lsp ON lsp.id = sp.ma_danh_muc
JOIN bien_the_san_pham bt ON bt.ma_san_pham = sp.id
WHERE sp.deleted_at IS NULL
  AND bt.deleted_at IS NULL
ORDER BY sp.id, bt.id;
```

### Xem đơn hàng mới nhất

```sql
SELECT
  id,
  ma_hien_thi_don,
  kenh_dat_hang,
  trang_thai,
  tong_tien_thanh_toan,
  ngay_tao
FROM don_hang
WHERE deleted_at IS NULL
ORDER BY ngay_tao DESC
LIMIT 20;
```

### Kiểm tra số lượng đơn theo trạng thái

```sql
SELECT trang_thai, COUNT(*) AS so_luong
FROM don_hang
WHERE deleted_at IS NULL
GROUP BY trang_thai
ORDER BY trang_thai;
```

## 6. Khi theo dõi production

Chỉ tạo connection production khi partner đã có thông tin host/port và quyền truy cập hợp lệ. Nên tạo user database chỉ có quyền đọc cho công việc quan sát.

- Đặt tên connection rõ ràng, ví dụ `MilkTea Production - READ ONLY`.
- Bật SSL nếu database provider yêu cầu.
- Không lưu mật khẩu production trong repository hoặc file `.env` được chia sẻ.
- Không chạy `DELETE`, `UPDATE`, `DROP`, `TRUNCATE`, hay sửa cấu trúc bảng trực tiếp.
- Với thay đổi schema, luôn tạo migration mới trong `src/main/resources/db/migration/` và deploy qua ứng dụng/Flyway.
- Backup database trước mọi thay đổi dữ liệu có chủ đích.

## 7. Lỗi thường gặp

| Lỗi | Cách xử lý |
| --- | --- |
| `Connection refused` | Chạy `docker compose ps`; đảm bảo service `postgres` đang chạy và port trong connection khớp `POSTGRES_PORT` của `.env`. |
| `password authentication failed` | Kiểm tra `POSTGRES_USER` và `POSTGRES_PASSWORD` trong `.env`. Nếu container đã tạo từ mật khẩu cũ, cần dùng mật khẩu cũ hoặc làm mới volume local theo quy trình trong tài liệu chạy local. |
| Không thấy bảng | Kiểm tra đang chọn database `milktea`, schema `public`; đồng thời khởi động backend ít nhất một lần để Flyway chạy migration. |
| Không kết nối được với host `postgres` | Đổi host thành `localhost` khi VS Code chạy ngoài Docker. |

## 8. Liên quan

- [Hướng dẫn chạy local và deploy](huong-dan-chay-local-va-deploy.md)
- [Kiến trúc phase 1–5](kien-truc-phase-1-5.md)

