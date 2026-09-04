package com.viettel.bccs.policy.mapactiveinfo.elasticsearch;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Document Elasticsearch cho bảng MapActiveInfo — mirror {@code MapActiveInfoEntity}, TRỪ
 * {@code FILE_ATTACH} (BLOB, không index được). Field {@code id} dùng {@link Long} — lưu ý cột thật
 * trong Oracle là {@code NUMBER(18,0)}, không phải precision=10 như entity JPA khai (metadata entity
 * bị lệch so với DB thật, xem ghi chú lúc rà soát) — {@link Long} đủ rộng nên không có rủi ro tràn số.
 *
 * <p>{@code stationCodes} được tách sẵn thành mảng ({@code stationCodesList}) lúc index, vì DB lưu
 * dạng chuỗi phân tách bằng {@code ';'} và {@code MapActiveInfoRepositoryCustomImpl.buildQuery}
 * matching kiểu {@code LIKE '%value;%'} — ES search theo {@code terms} trên mảng đã tách sẽ chính
 * xác và nhanh hơn nhiều so với mô phỏng LIKE trên 1 field text.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapActiveInfoEsDocument {

    public static final String INDEX_NAME = "map_active_info";

    private Long id;
    private Long telServiceId;
    private String productCode;
    private String productName;
    private Long regReasonId;
    private String reasonName;
    private String promCode;
    private String promName;
    private Long channelTypeId;
    private String channelName;
    private String provinceCode;
    private String districtCode;
    private Date effectDate;
    private Date endDate;
    private String provinceName;
    private String districtName;
    private Long offerId;
    private String offerName;
    private Long status;
    private String precinctName;
    private String precinctCode;
    private String shopCode;
    private String staffCode;
    private String actionCode;
    private String actionName;
    private String limitNumber;
    private Long captcharRequire;
    private String unit;
    private String customerGroup;
    private String customerType;
    private String subType;
    private String subGroup;
    private String policyDoc;
    private String actionGroup;
    private String actionGroupName;
    private String fileName;
    private Long stationId;
    private Long shopId;
    private String createUser;
    private Date issueDatetime;
    private String stationCodes;
    /** Mảng đã tách từ {@link #stationCodes} theo dấu ';', dùng cho search {@code terms}. */
    private List<String> stationCodesList;
    private String payType;
    private String technology;
    private Date updateDatetime;
    private String updateUser;
    private String areaGroupCode;
    private String vasCode;
    private String vasName;
    private String nodeCode;
    private String note;
    private String groupNodeCode;
    private String staffType;
    private String businessNo;
    private String subGroupCode;
    private String smeCustomerGroup;
    private Long importOfflineId;
}
