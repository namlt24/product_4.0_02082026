package com.bccs.gatewaymanager.dto;

/**
 * Thong tin doi dang thao tac - lay tu API key trong header X-Gateway-Admin-Key
 * cua request hien tai (ApiKeyAuthFilter tra nguoc key -> teamCode trong
 * gwm_team roi set CurrentTeamContext). Dung de UI hien badge "Team: xxx" tren
 * toolbar - khi nhieu doi dung chung 1 gateway thi nguoi dung phai nhin thay
 * minh dang la doi nao.
 */
public record CurrentTeamDto(
        String teamCode,
        String teamName
) {
}
