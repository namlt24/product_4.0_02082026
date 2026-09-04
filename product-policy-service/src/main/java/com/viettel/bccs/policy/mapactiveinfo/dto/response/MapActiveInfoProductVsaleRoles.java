package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import java.util.List;

import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record MapActiveInfoProductVsaleRoles(

        @Schema(description = "Vai trò bán hàng (Vsale role) áp dụng")
        OptionSetValueResponse onVsaleRole,

        @Schema(description = "Danh sách vai trò M2M")
        @Size(max = 100, message = "roleM2Ms tối đa 100 phần tử")
        List<String> roleM2Ms,

        @Schema(description = "Danh sách vai trò đại lý (DB)")
        @Size(max = 100, message = "roleDBs tối đa 100 phần tử")
        List<String> roleDBs,

        @Schema(description = "Danh sách vai trò gọi thưởng (GOITHUONG)")
        @Size(max = 100, message = "roleGoiThuongs tối đa 100 phần tử")
        List<String> roleGoiThuongs
) {
}
