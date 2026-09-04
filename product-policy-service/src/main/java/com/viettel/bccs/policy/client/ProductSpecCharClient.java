package com.viettel.bccs.policy.client;

import java.util.List;

import com.viettel.bccs.policy.client.dto.ProductSpecCharLookupDTO;
import com.viettel.bccs.policy.client.dto.ProductSpecCharValueLookupDTO;

public interface ProductSpecCharClient {

    List<ProductSpecCharLookupDTO> findByIds(List<Long> ids);

    List<ProductSpecCharValueLookupDTO> findValuesByIds(List<Long> ids);
}
