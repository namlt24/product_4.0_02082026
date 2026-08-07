package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.ProductSpecCharLookupDTO;
import com.viettel.bccs.policy.client.dto.ProductSpecCharValueLookupDTO;

import java.util.List;

public interface ProductSpecCharClient {

    List<ProductSpecCharLookupDTO> findByIds(List<Long> ids);

    List<ProductSpecCharValueLookupDTO> findValuesByIds(List<Long> ids);
}
