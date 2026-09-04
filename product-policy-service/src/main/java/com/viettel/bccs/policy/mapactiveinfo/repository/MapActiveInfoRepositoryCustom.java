package com.viettel.bccs.policy.mapactiveinfo.repository;

import java.util.List;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;

public interface MapActiveInfoRepositoryCustom {

    List<MapActiveInfoEntity> getListMapActiveInfoByNode(String nodeCode, List<Long> mapActiveInfoId) throws Exception;

    List<MapActiveInfoEntity> findByExample(MapActiveInfoDTO exampleMapActiveInfo, boolean searchInvidualField,
            boolean isTgdd) throws Exception;
}