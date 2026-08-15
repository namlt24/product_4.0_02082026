package com.viettel.bccs.productcatalog.productspecchar.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.mapper.ProductSpecCharMapper;
import com.viettel.bccs.productcatalog.productspecchar.repository.ProductSpecCharRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSpecCharService {

    private final ProductSpecCharRepository productSpecCharRepository;
    private final ProductSpecCharMapper productSpecCharMapper;

    @Transactional(readOnly = true)
    public ProductSpecCharResponse getByCode(String code) {
        return productSpecCharRepository.findByCode(code)
                .map(productSpecCharMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-CHAR-0001", "Product spec char not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public ProductSpecCharResponse getById(Long id) {
        return productSpecCharRepository.findById(id)
                .map(productSpecCharMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-CHAR-0001", "Product spec char not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductSpecCharResponse> getAll() {
        return productSpecCharRepository.findAll().stream()
                .map(productSpecCharMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSpecCharResponse> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productSpecCharRepository.findAllById(ids).stream()
                .filter(entity -> Const.STATUS.ACTIVE.equals(entity.getStatus()))
                .map(productSpecCharMapper::toResponse)
                .toList();
    }

    /**
     * Migrate từ mono: ExternalServiceForMbccsImpl.findProductOfferingByListCodeListSpecCode bước 1
     * — lấy các bản ghi product_spec_char (kèm product_offering chứa nó) theo lstSpecCode (bắt
     * buộc — rỗng thì trả rỗng ngay, không query), tuỳ chọn lọc thêm theo lstProductCode.
     *
     * <p>Trả {@code List<Object[]>} thay vì {@code List<ProductSpecCharDTO>} (khác tên gợi ý ban
     * đầu) vì mỗi dòng còn kèm product_offering_id/code/name — không có trong
     * {@code ProductSpecCharDTO} — cần cho bước gom nhóm theo offering ở
     * {@code ProductOfferingService.findProductOfferingByListCodeListSpecCode}.</p>
     */
    @Transactional(readOnly = true)
    public List<Object[]> findByLstSpecCodeAndLstProductCode(List<String> lstSpecCode, List<String> lstProductCode, Long productOfferTypeId) {
        if (DataUtil.isNullOrEmpty(lstSpecCode)) {
            return List.of();
        }
        return productSpecCharRepository.findByListSpecCodeAndListProductCode(lstSpecCode, lstProductCode, productOfferTypeId);
    }

}