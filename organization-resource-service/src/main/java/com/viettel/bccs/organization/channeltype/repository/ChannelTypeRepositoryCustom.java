package com.viettel.bccs.organization.channeltype.repository;

import java.util.List;

public interface ChannelTypeRepositoryCustom {

    /** Danh sách CHANNEL_TYPE_ID đang active, dùng để warm channelTypeCache. */
    List<Long> findActiveChannelTypeIds();
}
