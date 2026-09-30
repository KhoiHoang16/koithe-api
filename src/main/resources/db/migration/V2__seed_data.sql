INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES
  ('ADMIN', 'Quản trị hệ thống'), ('MANAGER', 'Quản lý cửa hàng'), ('CASHIER', 'Thu ngân'), ('BARISTA', 'Nhân viên pha chế'), ('CUSTOMER', 'Khách hàng');
INSERT INTO nguoi_dung (ma_vai_tro, ten_dang_nhap, mat_khau_ma_hoa, ho_va_ten)
VALUES ((SELECT id FROM vai_tro WHERE ten_vai_tro = 'ADMIN'), 'admin', '$2a$10$lJd5Veh6xXzuFkXbJmO5Y.o8FneFgkA5591or.aryh7QcTJMC6idm', 'Administrator');
INSERT INTO loai_san_pham (ten_danh_muc) VALUES ('Trà sữa'), ('Cà phê'), ('Đá xay');
