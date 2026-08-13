package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "staffCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Mã hành động", example = "00")
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    @Schema(description = "Danh sách ID sản phẩm", example = "[401027120]")
    @Size(max = 1000, message = "offerIds tối đa 1000 phần tử")
    private List<Long> offerIds;

    @Schema(description = "Mã khuyến mãi", example = "SBN")
    @Size(max = 50, message = "promotionCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promotionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String promotionCode;

    @NotNull(message = "regReasonId is required")
    @Schema(description = "ID lý do đăng ký", example = "9003998100")
    @Min(value = 0, message = "regReasonId phải >= 0")
    @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cột (precision 10)")
    private Long regReasonId;

    @Schema(description = "Đáp án CAPTCHA")
    @Size(max = 50, message = "captchaAnswer tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "captchaAnswer không được chứa ký tự điều khiển")
    private String captchaAnswer;

    @Schema(description = "ID dịch vụ viễn thông", example = "73")
    @Min(value = 0, message = "telServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
    private Long telServiceId;

    @Schema(description = "Mã sản phẩm", example = "VCONNECT_VBN0")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Ngày hiện tại")
    private Date nowDate;

    // Boolean (khong phai primitive boolean): project bat FAIL_ON_NULL_FOR_PRIMITIVES, neu client
    // khong truyen field nay se gay HttpMessageNotReadableException (400) o tang deserialize thay
    // vi duoc hieu ngam la false. Doi sang Boolean de field thuc su optional; noi doc gia tri phai
    // dung Boolean.TRUE.equals(...) de tranh NPE khi null (xem MapActiveInfoValidateController).
    @Schema(description = "Có cần kiểm tra CAPTCHA", example = "false")
    private Boolean isNeedCheckCaptcha;

    @Schema(description = "Mã tỉnh/thành phố", example = "HCM")
    @Size(max = 50, message = "province tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "province chỉ gồm chữ, số, '_' hoặc '-'")
    private String province;

    @Schema(description = "Mã quận/huyện", example = "")
    @Size(max = 50, message = "district tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "district chỉ gồm chữ, số, '_' hoặc '-'")
    private String district;

    @Schema(description = "Mã phường/xã", example = "")
    @Size(max = 50, message = "precinct tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "precinct chỉ gồm chữ, số, '_' hoặc '-'")
    private String precinct;

    @Schema(description = "Nhóm khách hàng", example = "1")
    @Size(max = 10, message = "customerGroup tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerGroup chỉ gồm chữ, số, '_' hoặc '-'")
    private String customerGroup;

    @Schema(description = "Loại khách hàng", example = "CQU")
    @Size(max = 10, message = "customerType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerType chỉ gồm chữ, số, '_' hoặc '-'")
    private String customerType;

    @Schema(description = "Loại phụ", example = "")
    @Size(max = 4, message = "subType tối đa 4 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,4}$", message = "subType chỉ gồm chữ, số, '_' hoặc '-'")
    private String subType;

    @Schema(description = "Nhóm phụ", example = "")
    @Size(max = 10, message = "subGroup tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "subGroup chỉ gồm chữ, số, '_' hoặc '-'")
    private String subGroup;

    @Schema(description = "Mã trạm", example = "")
    @Size(max = 1000, message = "stationCodes tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "stationCodes không được chứa ký tự điều khiển")
    private String stationCodes;

    @Schema(description = "Loại thanh toán", example = "1")
    @Size(max = 1, message = "payType đúng 1 ký tự")
    @Pattern(regexp = "^[0-9]{0,1}$", message = "payType chỉ nhận 1 chữ số")
    private String payType;

    @Schema(description = "Công nghệ", example = "")
    @Size(max = 10, message = "technology tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "technology chỉ gồm chữ, số, '_' hoặc '-'")
    private String technology;

    @Schema(description = "Chế độ", example = "1")
    @Min(value = 0, message = "mode phải >= 0")
    @Max(value = 9, message = "mode phải <= 9")
    private int mode;

    @Schema(description = "Loại sản phẩm", example = "")
    @Size(max = 20, message = "productOfferType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "productOfferType chỉ gồm chữ, số, '_' hoặc '-'")
    private String productOfferType;

    @Schema(description = "Danh sách số kinh doanh", example = "[\"-1\"]")
    @Size(max = 1000, message = "lstBusinessNo tối đa 1000 phần tử")
    private List<String> lstBusinessNo;
}