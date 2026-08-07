package com.viettel.bccs.productcatalog.telecomservice.repository;

import com.viettel.bccs.productcatalog.telecomservice.entity.TelecomServiceEntity;

public interface TelecomServiceRepositoryCustom {

    TelecomServiceEntity getTelServiceByAlias(String alias);
}
