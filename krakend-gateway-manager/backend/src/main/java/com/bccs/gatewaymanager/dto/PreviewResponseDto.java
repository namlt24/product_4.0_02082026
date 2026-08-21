package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Ket qua preview krakend.json (mot phan hoac toan bo).
 * warnings: canh bao nghiep vu (vi du mapping khong hop le, header chain
 * khong duoc KrakenD CE ho tro...) - KHONG lam fail request, chi de UI hien
 * banner canh bao truoc khi nguoi dung bam Deploy.
 */
public record PreviewResponseDto(
        JsonNode json,
        List<String> warnings
) {
}
