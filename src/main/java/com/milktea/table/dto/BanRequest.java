package com.milktea.table.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BanRequest(
        @JsonAlias({"tableNumber", "so_ban"}) String soBan,
        @JsonAlias({"qrToken", "ma_qr_token"}) String maQrToken,
        @JsonAlias({"status", "trang_thai"}) String trangThai) {
}
