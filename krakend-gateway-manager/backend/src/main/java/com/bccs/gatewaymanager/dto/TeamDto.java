package com.bccs.gatewaymanager.dto;

import java.time.Instant;

/** Hien thi 1 doi trong danh sach "Quan ly doi" - KHONG bao gio kem apiKey (chi hien 1 lan luc tao, xem TeamCreatedDto). */
public record TeamDto(
        String teamCode,
        String teamName,
        Instant createdAt
) {
}
