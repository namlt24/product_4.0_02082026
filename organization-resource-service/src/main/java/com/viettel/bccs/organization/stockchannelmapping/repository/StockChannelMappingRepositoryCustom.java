package com.viettel.bccs.organization.stockchannelmapping.repository;

import java.util.Date;
import java.util.List;

public interface StockChannelMappingRepositoryCustom {

    List<Long> findActiveFunctionalStockIds(Long telServiceId, Long channelTypeId, Long shopId, Long staffId,
            Date today, String status);

}
