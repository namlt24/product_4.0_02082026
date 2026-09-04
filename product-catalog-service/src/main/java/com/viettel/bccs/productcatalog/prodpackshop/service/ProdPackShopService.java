package com.viettel.bccs.productcatalog.prodpackshop.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.prodpackshop.mapper.ProdPackShopMapper;
import com.viettel.bccs.productcatalog.prodpackshop.repository.ProdPackShopRepository;
import com.viettel.bccs.productcatalog.utils.DataUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProdPackShopService {

    private final ProdPackShopRepository repository;
    private final ProdPackShopMapper mapper;

    /**
     * Tìm danh sách shopId theo danh sách prodPackTypeId từ bảng PROD_PACK_SHOP.
     * Cache key dựa trên chuỗi sorted các prodPackTypeIds để đảm bảo consistency.
     *
     * @param prodPackTypeIds danh sách prodPackTypeId cần lọc, chỉ lấy các bản ghi có status = 1
     * @return Map với key = prodPackTypeId, value = danh sách shopId tương ứng
     */
//    @Cacheable(value = "prodPackShopCache", key = "'SHOP_IDS:' + T(String).join(',',
  // #prodPackTypeIds.stream().sorted().toList())")
    public Map<Long, List<Long>> findShopIdsByProdPackTypeIds(List<Long> prodPackTypeIds) {
        if (DataUtil.isNullOrEmpty(prodPackTypeIds)) {
            return Map.of();
        }
        return repository.findShopIdsByProdPackTypeIds(prodPackTypeIds);
    }
}