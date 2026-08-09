package com.viettel.bccs.productcatalog.telecomservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Dịch vụ viễn thông")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TelecomServiceDTO implements Serializable {

    @Schema(description = "ID dịch vụ viễn thông")
    private Long telecomServiceId;

    @Schema(description = "Tên dịch vụ viễn thông", example = "Di động")
    private String name;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Mã alias dịch vụ", example = "MOB")
    private String serviceAlias;

    @Schema(description = "Người tạo")
    private String createUser;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Người cập nhật")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;
}
