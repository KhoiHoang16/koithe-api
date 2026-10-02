# Danh mục các trường Enum/Trạng thái trong hệ thống

Các giá trị dưới đây hiện được lưu dưới dạng `VARCHAR`/boolean, chưa được ràng buộc bằng Java `enum` hoặc `CHECK` constraint đầy đủ. Khi bổ sung giá trị, cập nhật tài liệu này và kiểm tra API, validation, seed data, cùng các luồng chuyển trạng thái.

## `vai_tro.ten_vai_tro`

- `ADMIN`: Quản trị hệ thống.
- `MANAGER`: Quản lý cửa hàng.
- `CASHIER`: Thu ngân.
- `BARISTA`: Nhân viên pha chế.
- `CUSTOMER`: Khách hàng.

## Các cờ hoạt động/xác thực

### `nguoi_dung.dang_hoat_dong`

- `true`: Tài khoản đang hoạt động.
- `false`: Tài khoản bị vô hiệu hóa.

### `chuong_trinh_khuyen_mai.dang_hoat_dong`

- `true`: Chương trình được bật; vẫn phải kiểm tra khoảng thời gian hiệu lực.
- `false`: Chương trình bị tắt.

### `nha_cung_cap.dang_hop_tac`

- `true`: Nhà cung cấp đang hợp tác.
- `false`: Nhà cung cấp đã ngừng hợp tác.

### `khach_hang.da_xac_thuc`

- `true`: Khách hàng đã xác thực.
- `false`: Khách hàng chưa xác thực.

## `ca_lam_viec.trang_thai`

- `DANG_MO`: Ca đang mở.
- `DA_DONG`: Ca đã đóng.

Luồng dự kiến: tạo ca ở `DANG_MO`, đóng ca chuyển sang `DA_DONG`; chỉ ca đang mở mới được đóng.

## `don_hang.kenh_dat_hang`

- `TAI_QUAY_POS`: Đặt tại quầy POS.
- `QUET_QR_BAN`: Khách quét QR tại bàn.
- `UNG_DUNG_APP`: Đặt qua ứng dụng di động.
- `WEBSITE`: Đặt qua website.

## `don_hang.loai_phuc_vu`

- `TAI_CHO`: Dùng tại quán.
- `MANG_VE`: Mang đi.
- `GIAO_HANG`: Giao hàng.

## `don_hang.trang_thai`

Các chuyển trạng thái hiện được cho phép trong `OrderServiceImpl`:

- `CHO_XAC_NHAN` → `DA_THANH_TOAN` hoặc `DA_HUY`.
- `DA_THANH_TOAN` → `DANG_PHA_CHE` hoặc `DA_HUY`.
- `DANG_PHA_CHE` → `SAN_SANG` hoặc `DA_HUY`.
- `SAN_SANG` → `HOAN_THANH` hoặc `DA_HUY`.
- `HOAN_THANH` và `DA_HUY` là trạng thái kết thúc theo service hiện tại.

## `giao_dich_thanh_toan.phuong_thuc_thanh_toan`

- `TIEN_MAT`: Thu tiền mặt tại quầy.
- `CHUYEN_KHOAN_QR`: Chuyển khoản bằng QR/VietQR.
- `MOMO`: Thanh toán qua MoMo.
- `VNPAY`: Thanh toán qua VNPay.
- `THE_NGAN_HANG`: Thanh toán bằng thẻ ngân hàng.

## `giao_dich_thanh_toan.trang_thai`

- `CHO_XU_LY`: Giao dịch đã tạo, đang chờ kết quả.
- `THANH_CONG`: Giao dịch thành công.
- `THAT_BAI`: Giao dịch thất bại.
- `HOAN_TIEN`: Giao dịch đã được hoàn tiền.

## `khuyen_mai_*.loai_giam_gia`

- `PHAN_TRAM`: Giảm theo phần trăm; áp dụng `muc_giam_toi_da` nếu có.
- `TIEN_CO_DINH`: Giảm một số tiền cố định.

## `phieu_nhap_hang.trang_thai`

- `CHO_DUYET`: Phiếu chờ duyệt; chỉ trạng thái này được sửa hoặc hủy theo luồng dự kiến.
- `DA_NHAP_KHO`: Phiếu đã duyệt và tồn kho đã được cập nhật.
- `DA_HUY`: Phiếu đã hủy.

**Lưu ý schema:** migration V1 đặt mặc định `DA_NHAP_KHO`, trong khi quy trình duyệt cần phiếu mới ở `CHO_DUYET`. Không sửa migration đã chạy; service tạo phiếu cần chủ động gán `CHO_DUYET` trước khi lưu.

## `ban.trang_thai`

- `TRONG`: Bàn trống.
- `DANG_CO_KHACH`: Bàn đang có khách.
- `DA_DAT_TRUOC`: Bàn đã được đặt trước.

## `khach_hang.hang_thanh_vien`

- `DONG`: Hạng đồng (mặc định).
- `BAC`: Hạng bạc.
- `VANG`: Hạng vàng.
- `KIM_CUONG`: Hạng kim cương.

Ngưỡng điểm và điều kiện nâng/hạ hạng cần được Product Owner xác nhận trước khi triển khai.
