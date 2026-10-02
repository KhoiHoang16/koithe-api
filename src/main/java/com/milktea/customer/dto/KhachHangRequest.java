package com.milktea.customer.dto;

import java.time.Instant;

public record KhachHangRequest(String soDienThoai, String email, String hoVaTen, String matKhauMaHoa,
        Integer diemTichLuy, String hangThanhVien, String tokenLamMoi, Boolean daXacThuc, Instant ngayTao) {
}
