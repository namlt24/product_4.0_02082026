package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request validate MapActiveInfo",
        example = """
                {
                  "staffCode": "HUONGTTN2_GDV_DVTM_HCM",
                  "actionCode": "00",
                  "offerIds": [401027120],
                  "promotionCode": "SBN",
                  "regReasonId": 9003998100,
                  "captchaAnswer": "",
                  "telServiceId": 73,
                  "productCode": "VCONNECT_VBN0",
                  "nowDate": null,
                  "isNeedCheckCaptcha": false,
                  "province": "",
                  "district": "",
                  "precinct": "",
                  "customerGroup": "1",
                  "customerType": "CQU",
                  "subType": "",
                  "subGroup": "",
                  "stationCodes": "",
                  "payType": "1",
                  "technology": "",
                  "mode": 1,
                  "lstBusinessNo": ["-1"]
                }""")
public class ValidateInputMapActiveInfoRequest {

    @Schema(description = "Mã nhân viên", example = "HUONGTTN2_GDV_DVTM_HCM")
    private String staffCode;

    @Schema(description = "Mã hành động", example = "00")
    private String actionCode;

    @Schema(description = "Danh sách ID sản phẩm", example = "[401027120]")
    private List<Long> offerIds;

    @Schema(description = "Mã khuyến mãi", example = "SBN")
    private String promotionCode;

    @NotNull(message = "regReasonId is required")
    @Schema(description = "ID lý do đăng ký", example = "9003998100")
    private Long regReasonId;

    @Schema(description = "Đáp án CAPTCHA")
    private String captchaAnswer;

    @Schema(description = "ID dịch vụ viễn thông", example = "73")
    private Long telServiceId;

    @Schema(description = "Mã sản phẩm", example = "VCONNECT_VBN0")
    private String productCode;

    @Schema(description = "Ngày hiện tại")
    private Date nowDate;

    @Schema(description = "Có cần kiểm tra CAPTCHA", example = "false")
    private boolean isNeedCheckCaptcha;

    @Schema(description = "Mã tỉnh/thành phố", example = "HCM")
    private String province;

    @Schema(description = "Mã quận/huyện", example = "")
    private String district;

    @Schema(description = "Mã phường/xã", example = "")
    private String precinct;

    @Schema(description = "Nhóm khách hàng", example = "1")
    private String customerGroup;

    @Schema(description = "Loại khách hàng", example = "CQU")
    private String customerType;

    @Schema(description = "Loại phụ", example = "")
    private String subType;

    @Schema(description = "Nhóm phụ", example = "")
    private String subGroup;

    @Schema(description = "Mã trạm", example = "")
    private String stationCodes;

    @Schema(description = "Loại thanh toán", example = "1")
    private String payType;

    @Schema(description = "Công nghệ", example = "")
    private String technology;

    @Schema(description = "Chế độ", example = "1")
    private int mode;

    @Schema(description = "Loại sản phẩm", example = "")
    private String productOfferType;

    @Schema(description = "Danh sách số kinh doanh", example = "[\"-1\"]")
    private List<String> lstBusinessNo;
}