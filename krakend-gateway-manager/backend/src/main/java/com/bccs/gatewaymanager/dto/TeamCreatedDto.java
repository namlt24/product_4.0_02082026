package com.bccs.gatewaymanager.dto;

import java.time.Instant;

/**
 * Tra ve DUNG 1 LAN ngay sau khi tao doi moi (kem apiKey plaintext) - giong
 * pattern token ca nhan cua GitHub: sau lan nay, apiKey KHONG con hien lai o
 * bat ky API nao khac (TeamDto/list() khong co field nay) - neu mat, chi con
 * cach xoa doi va tao lai (V1 chua co "regenerate key" rieng).
 */
public record TeamCreatedDto(
        String teamCode,
        String teamName,
        String apiKey,
        Instant createdAt
) {
}
