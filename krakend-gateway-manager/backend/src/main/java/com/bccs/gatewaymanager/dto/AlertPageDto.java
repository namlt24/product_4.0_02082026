package com.bccs.gatewaymanager.dto;

import java.util.List;

/** Ket qua tim kiem 1 trang cua GWM_ALERT - dung cho trang "Canh bao" (giong LogSearchResultDto). */
public record AlertPageDto(
        List<AlertResponseDto> items,
        long total,
        int page,
        int size
) {
}
