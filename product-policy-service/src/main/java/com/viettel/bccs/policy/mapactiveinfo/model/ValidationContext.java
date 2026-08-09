package com.viettel.bccs.policy.mapactiveinfo.model;

import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;

import java.util.List;
import java.util.Map;

/**
 * Các giá trị không đổi theo từng offerId trong 1 lần gọi validateMapActiveInfo:
 * staff/shop, cấu hình option set liên quan, channelType, và tỉnh/huyện/phường cuối cùng
 * (đã áp dụng cả override theo staffExt nếu có) dùng để build mapActiveInfoExample.
 */
public record ValidationContext(Map<String, List<OptionSetValueResponse>> optionSetMap, Long chanelTypeId,
                                 String provinceID, String districtID, String precintId) {
}
