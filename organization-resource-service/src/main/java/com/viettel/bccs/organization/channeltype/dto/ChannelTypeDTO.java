package com.viettel.bccs.organization.channeltype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class ChannelTypeDTO {

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh", example = "Đại lý")
    private String name;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    private String status;

    @Schema(description = "Loại đối tượng: 1 shop, 2 staff", example = "1")
    private String objectType;

    @Schema(description = "Thuộc Viettel: 1 Thuộc VT, 2 Không thuộc Viettel", example = "1")
    private String isVtUnit;

    @Schema(description = "Tính hoa hồng: 1 Tính, 2 Không tính", example = "1")
    private String checkComm;

    @Schema(description = "Loại kho thao tác: 1 shop, 2 staff", example = "1")
    private Integer stockType;

    @Schema(description = "Mẫu phiếu xuất nhập kho", example = "TEMPLATE_001")
    private String stockReportTemplate;

    @Schema(description = "Số lần đặt cọc tối đa", example = "10")
    private Long totalDebit;

    @Schema(description = "Cho phép thêm batch", example = "1")
    private Integer allowAddBatch;

    @Schema(description = "Mã đối tượng suffix", example = "VTST")
    private String suffixObjectCode;

    @Schema(description = "Role cập nhật staff owner", example = "ROLE_ADMIN")
    private String updateStaffOwnerRole;

    @Schema(description = "Mã chính sách giảm giá mặc định", example = "DISC_DEFAULT")
    private String discountPolicyDefaut;

    @Schema(description = "Mã chính sách giá mặc định", example = "PRICE_DEFAULT")
    private String pricePolicyDefaut;

    @Schema(description = "Role cập nhật mã trắng", example = "ROLE_BLANK")
    private String updateBlankCodeRole;

    @Schema(description = "Role cập nhật thông tin đối tượng", example = "ROLE_OBJ_INFO")
    private String updateObjectInfoRole;

    @Schema(description = "Role cập nhật cửa hàng", example = "ROLE_SHOP")
    private String updateShopRole;

    @Schema(description = "Mã loại kênh", example = "CT01")
    private String code;

    @Schema(description = "Mã nhóm loại kênh", example = "5")
    private Long groupChannelTypeId;

    @Schema(description = "Mã nhóm kênh", example = "10")
    private Long groupChannelId;

    @Schema(description = "Trường xác định kênh VHR: 1 Lấy từ VHR", example = "0")
    private Integer isVhrChannel;

    @Schema(description = "Kênh thu hộ", example = "1")
    private Integer isCollChannel;

    @Schema(description = "Không tạo mã trắng", example = "1")
    private Integer isNotBlankCode;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;

    @Schema(description = "Mã thanh toán", example = "PAY001")
    private String paymentCode;

    @Schema(description = "Hậu tố thanh toán", example = "TAIL")
    private String paymentTail;

    @Schema(description = "Giao khách hàng", example = "1")
    private Integer assignCustStatus;

    @Schema(description = "Mô tả", example = "Kênh bán hàng Viettel Store")
    private String description;
}