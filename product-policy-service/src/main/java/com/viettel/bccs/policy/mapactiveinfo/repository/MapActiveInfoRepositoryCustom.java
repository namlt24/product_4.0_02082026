package com.viettel.bccs.policy.mapactiveinfo.repository;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;

import java.util.List;

public interface MapActiveInfoRepositoryCustom {

    List<MapActiveInfoEntity> getListMapActiveInfoByNode(String nodeCode, List<Long> mapActiveInfoId) throws Exception;

    List<MapActiveInfoEntity> findByExample(MapActiveInfoDTO exampleMapActiveInfo, boolean searchInvidualField, boolean isTgdd) throws Exception;
}