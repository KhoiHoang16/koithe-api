package com.milktea.customer.dto;

import com.milktea.customer.entity.HangThanhVien;
import java.time.Instant;

public record KhachHangResponse(
    Long id,
    String soDienThoai,
    String hoVaTen,
    String email,
    Integer diemTichLuy,
    HangThanhVien hangThanhVien,
    Boolean daXacThuc,
    Instant ngayTao,
    Instant createdAt,
    Instant updatedAt
) {}
