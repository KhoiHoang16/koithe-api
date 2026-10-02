package com.milktea.inventory.dto;

import java.time.Instant;

public record NhaCungCapRequest(String tenNhaCungCap, String soDienThoai, String email, String diaChi, String maSoThue,
        String nguoiDaiDien, Boolean dangHopTac, Instant ngayTao) {
}
