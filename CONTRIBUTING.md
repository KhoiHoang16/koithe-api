# 🤝 CONTRIBUTING — Quy tắc đặt tên nhánh và commit

Chào mừng partner đến với MilkTea! Mỗi partner phụ trách một module để công việc gọn và dễ review. Hãy dùng quy ước đơn giản dưới đây khi tạo nhánh và ghi commit.

## 1. Quy tắc đặt tên nhánh
### Format
```text
feature /<module>
```

### Tên module hợp lệ
`payment`, `promotion`, `shift`, `table`, `customer`, `inventory`, `report`.
### Ví dụ đúng
```text
feature/payment
feature/promotion
feature/shift
feature/payment
feature/payment
```

### Quy tắc
- Viết toàn bộ bằng chữ thường.
- Dùng dấu / phân tách type và module.
- Module phải khớp đúng danh sách hợp lệ ở trên.

## 2. Quy tắc commit
### Format
```text
<type>(<module>): <mô tả ngắn>
```
### Bảng type
| Type | Khi nào dùng |
|:---|:---|
| feat | Thêm tính năng mới |
| fix | Sửa bug |
| refactor | Refactor code, không đổi behavior |
| test | Thêm hoặc sửa test |
| docs | Cập nhật tài liệu |
| chore | Config hoặc build |
### Quy tắc mô tả
- Dùng thì hiện tại, giọng mệnh lệnh: add, fix, update; không dùng added/fixed.
- Bắt đầu subject bằng chữ thường.
- Không đặt dấu chấm cuối câu.
- Tối đa 72 ký tự.
- Nếu commit chạm nhiều module, dùng module chính làm scope.
- Tài liệu hoặc chore chung không gắn với module dùng scope riêng: docs: ... hoặc chore: ...
### Ví dụ đúng
```text
feat(payment): add VNPay gateway implementation
feat(payment): handle callback from Momo
fix(promotion): validate voucher expiration date
fix(shift): prevent opening two shifts at once
refactor(inventory): extract stock update logic
test(report): add unit test for revenue query
docs(payment): update TODO comment for VNPay
docs: update README
chore: update Maven configuration
```
### Ví dụ sai
```text
feat(payment): Added VNPay.   # thì quá khứ, viết hoa, có dấu chấm
update code                   # thiếu type và module
Fix bug thanh toán            # viết hoa và có dấu
feat: add VNPay                # thiếu module
WIP                            # không mô tả thay đổi
```
```

Khi module hoàn tất, chạy `mvn clean verify` và xác nhận PASS, sau đó mở Pull Request từ nhánh module vào develop và gán Tech Lead review.


