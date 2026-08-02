package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import com.viettel.bccs.policy.client.dto.StaffDTO;
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
                  "staffDTO": {
                    "channelTypeId": 14,
                    "email": "huongttn2@viettel.com.vn",
                    "isdn": "0963670127",
                    "name": "Trần Thị Ngọc Hường",
                    "shopId": 1413284,
                    "staffCode": "HUONGTTN2_GDV_DVTM_HCM",
                    "staffId": 110734881,
                    "staffOwnerId": 110745320,
                    "tel": "0963670127",
                    "tenantId": 1411861,
                    "shopCode": "1600340054",
                    "shopName": "Cửa hàng Viettel Cộng Hòa Hồ Chí Minh",
                    "isPointOfSale": false,
                    "shopChanelTypeId": 164,
                    "shopProvince": "HCM",
                    "shopPrecinct": "126",
                    "shopPath": "72811411861_1411895_1412079_1413284",
                    "ipAddress": "10.231.22.150",
                    "tablePk": "HUONGTTN2_GDV_DVTM_HCM"
                  },
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

    @Schema(description = "Thông tin nhân viên")
    private StaffDTO staffDTO;

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