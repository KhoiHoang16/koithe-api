package com.milktea.inventory.dto;

public record NhaCungCapResponse(Long id, String tenNhaCungCap, String soDienThoai, String email, String diaChi,
        String maSoThue, String nguoiDaiDien, Boolean dangHopTac) {
}
