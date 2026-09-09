package com.bccs.gatewaymanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeamCreateRequestDto(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$", message = "teamCode chi gom chu/so/gach ngang/gach duoi, 2-50 ky tu")
        String teamCode,

        @NotBlank
        String teamName
) {
}
