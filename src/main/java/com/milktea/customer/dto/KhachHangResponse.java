package com.milktea.customer.dto;

import java.time.Instant;

public record KhachHangResponse(Long id, String soDienThoai, String email, String hoVaTen, Integer diemTichLuy,
        String hangThanhVien, Boolean daXacThuc, Instant ngayTao) {
}
