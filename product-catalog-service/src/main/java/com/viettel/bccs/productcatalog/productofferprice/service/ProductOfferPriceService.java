package com.viettel.bccs.productcatalog.productofferprice.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.client.FreeCamEquipmentClient;
import com.viettel.bccs.productcatalog.client.MappingClient;
import com.viettel.bccs.productcatalog.client.SensorFreeClient;
import com.viettel.bccs.productcatalog.client.dto.FreeCamEquipmentDTO;
import com.viettel.bccs.productcatalog.client.dto.ReasonDTO;
import com.viettel.bccs.productcatalog.client.dto.SensorFeeRuleDTO;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.product.service.ProductOfferingService;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;
import com.viettel.bccs.productcatalog.productofferprice.mapper.ProductOfferPriceMapper;
import com.viettel.bccs.productcatalog.productofferprice.repository.ProductOfferPriceRepository;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.service.ProductPackageService;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.productspeccharvalue.service.ProductSpecCharValueService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductOfferPriceService {
    private static final String ERROR_CODE_EQUIPMENT_MORE_THAN_ONE = "BCCS-CATALOG-PRICE-0001";
    private static final String OPTION_SET_ON_CAM = "ON_CAM_EQUIPMENT_PRICE";
    private static final String OPTION_VALUE_ON = "1";
    private static final String DEVICE_TYPE_INDOOR = "1";
    private static final String DEVICE_TYPE_OUTDOOR = "2";
    private static final String DEVICE_TYPE_CAM_CHAR_CODE = "DEVICE_TYPE_CAM";

    private final ProductOfferPriceRepository productOfferPriceRepository;
    private final ProductOfferPriceMapper productOfferPriceMapper;
    private final ProductPackageService productPackageService;
    private final ProductOfferingRepository productOfferingRepository;
    private final ProductOfferingMapper productOfferingMapper;
    private final MappingClient mappingClient;
    private final SensorFreeClient sensorFreeClient;
    private final FreeCamEquipmentClient freeCamEquipmentClient;

    private final ProductOfferingService productOfferingService;
    private final OptionSetValueService optionSetValueService;
    private final ProductOfferPriceMapper mapper;
    private final ProductOfferPriceRepository repository;
    private final ProductSpecCharValueService productSpecCharValueService;

    @Transactional(readOnly = true)
    public ProductOfferPriceDTO getById(Long prodOfferPriceId) {
        return productOfferPriceRepository.findById(prodOfferPriceId)
                .map(productOfferPriceMapper::toDto)
                .orElse(null);
    }

    @Cacheable(value = "productOfferPriceCache",
            key = "'PCCC:' + #productPackageId + ':' + #productPackageCode + ':' + #productOfferType + ':' + #productOfferId + ':' + #pricePolicy")
    public List<ProductOfferPriceDTO> getPriceInServicesForPCCC(Long productPackageId, String productPackageCode,
                                                                Long productOfferType, Long productOfferId, Long pricePolicy) {
        Long temp = productPackageId;

        if (DataUtil.isNullOrEmpty(productPackageId)) {
            if (DataUtil.isNullOrEmpty(productPackageCode)) {
                return null;
            }
            ProductPackageDTO packageDTO = productPackageService.getActiveProductPackage(productPackageCode);
            if (!DataUtil.isNullObject(packageDTO)) {
                temp = packageDTO.getProductPackageId();
            }
        }

        if (DataUtil.isNullOrEmpty(temp)) {
            return null;
        }

        if (DataUtil.isNullOrZero(productOfferId)) {
            return null;
        }
        ProductOfferingDTO productOfferingDTO = productOfferingRepository.findById(productOfferId)
                .map(productOfferingMapper::toDto)
                .orElse(null);
        if (DataUtil.isNullObject(productOfferingDTO)) {
            return null;
        }

        if (DataUtil.isNullOrZero(pricePolicy)) {
            return null;
        }
        if (DataUtil.isNullOrZero(productOfferType)) {
            return null;
        }

        List<ProductOfferPriceDTO> lstResult = productOfferPriceMapper.toDtoBean(
                productOfferPriceRepository.getPriceInServices(temp, productOfferType, productOfferId, pricePolicy));

        if (!DataUtil.isNullOrEmpty(lstResult)) {
            for (ProductOfferPriceDTO dto : lstResult) {
                dto.setProductOfferName(productOfferingDTO.getName());
            }
        }

        List<ReasonDTO> lstMappingReason = mappingClient.getMappingReasonProductOfferPrice(temp);
        if (DataUtil.notNullOrEmpty(lstMappingReason)) {
            List<SensorFeeRuleDTO> listSensorFree = sensorFreeClient.checkReasonSensorFee(temp);
            if (DataUtil.notNullOrEmpty(listSensorFree)) {
                Long price = listSensorFree.get(0).getPromotionalPrice();
                if (DataUtil.isNullOrEmpty(lstResult)) {
                    lstResult = new ArrayList<>();
                    lstResult.add(new ProductOfferPriceDTO());
                }
                for (ProductOfferPriceDTO dto : lstResult) {
                    dto.setPriceEquipment(price);
                }
            }
        }

        return DataUtil.isNullOrEmpty(lstResult) ? null : lstResult;
    }

    // Migrate từ mono: ProductOfferPriceServiceImpl.getPriceInServices (ProductOfferPriceServiceImpl.java:320-408).
    // Bảng ánh xạ line-by-line: xem plan tại C:\Users\Admin\.claude\plans\humble-knitting-waterfall.md
    //    @Cacheable(value = "productOfferPriceCache",
//            key = "'GET_PRICE_IN_SERVICES:' + #productPackageId + ':' + #productPackageCode + ':' + #productOfferType + ':' + #productOfferId + ':' + #pricePolicy")
    public List<ProductOfferPriceResponse> getPriceInServices(Long productPackageId, String productPackageCode,
                                                              Long productOfferType, Long productOfferId, Long pricePolicy) {
        // L322-334 (legacy dùng isNullOrZero, KHÔNG phải isNullOrEmpty như bản getPriceInServicesForPCCC bên trên)
        Long temp = productPackageId;
        if (DataUtil.isNullOrZero(productPackageId)) {
            if (DataUtil.isNullOrEmpty(productPackageCode)) {
                return null;
            }
            ProductPackageDTO packageDTO = productPackageService.getActiveProductPackage(productPackageCode);
            if (!DataUtil.isNullObject(packageDTO)) {
                temp = packageDTO.getProductPackageId();
            }
        }
        if (DataUtil.isNullOrZero(temp)) {
            return null;
        }

        // L336-342
        if (DataUtil.isNullOrZero(productOfferId)) {
            return null;
        }
        ProductOfferingDTO productOfferingDTO = productOfferingRepository.findById(productOfferId)
                .map(productOfferingMapper::toDto)
                .orElse(null);
        if (DataUtil.isNullObject(productOfferingDTO)) {
            return null;
        }

        // L343-348
        if (DataUtil.isNullOrZero(pricePolicy)) {
            return null;
        }
        if (DataUtil.isNullOrZero(productOfferType)) {
            return null;
        }

        // L349-355
        List<ProductOfferPriceResponse> lstResult = productOfferPriceMapper.toResponseList(
                productOfferPriceRepository.getPriceInServices(temp, productOfferType, productOfferId, pricePolicy));
        if (!DataUtil.isNullOrEmpty(lstResult)) {
            List<ProductOfferPriceResponse> withName = new ArrayList<>();
            for (ProductOfferPriceResponse dto : lstResult) {
                withName.add(productOfferPriceMapper.withPriceInfo(dto, productOfferingDTO.getName(),
                        dto.priceEquipment(), dto.priceEquipmentId(), dto.priceEquipmentTypeId()));
            }
            lstResult = withName;
        }

        // L356-403
        List<ReasonDTO> lstMappingReason = mappingClient.getMappingReasonProductOfferPrice(temp);
        if (DataUtil.notNullOrEmpty(lstMappingReason)) {
            List<FreeCamEquipmentDTO> lstFreeCamEquipment = freeCamEquipmentClient.checkReasonFreeCam(temp);
            if (DataUtil.notNullOrEmpty(lstFreeCamEquipment)) {
                var optionSetValueDTO = optionSetValueService.findOneByCodeAndValue(OPTION_SET_ON_CAM, OPTION_VALUE_ON);
                if (DataUtil.notNullObject(optionSetValueDTO)) {
                    lstResult = applyCamEquipmentPrice(lstResult, lstFreeCamEquipment, productOfferId);
                } else {
                    lstResult = applyNormalEquipmentPrice(lstResult, temp, productOfferType, productOfferId);
                }
            }
        }

        // L406
        return DataUtil.isNullOrEmpty(lstResult) ? null : lstResult;
    }

    /**
     * Mục "L361-387" trong bảng ánh xạ: nhánh CAM equipment. Lấy spec char DEVICE_TYPE_CAM của
     * mặt hàng, dò trong danh sách FreeCamEquipment để chọn camInsidePrice/camOutsidePrice tương ứng.
     */
    private List<ProductOfferPriceResponse> applyCamEquipmentPrice(List<ProductOfferPriceResponse> lstResult,
                                                                    List<FreeCamEquipmentDTO> lstFreeCamEquipment,
                                                                    Long productOfferId) {
        List<ProductSpecCharValueEntity> lstProductSpecChar = productSpecCharValueService
                .getByProductSpecCharCodeAndProductOfferingId(DEVICE_TYPE_CAM_CHAR_CODE, productOfferId);
        if (DataUtil.isNullOrEmpty(lstProductSpecChar)) {
            return lstResult;
        }
        ProductSpecCharValueEntity specCharValue = lstProductSpecChar.get(0);
        Long price = null;
        for (FreeCamEquipmentDTO item : lstFreeCamEquipment) {
            if (DataUtil.safeEqual(specCharValue.getValue(), DEVICE_TYPE_INDOOR) && item.getCamInsidePrice() != null) {
                price = item.getCamInsidePrice().longValue();
                break;
            }
            if (DataUtil.safeEqual(specCharValue.getValue(), DEVICE_TYPE_OUTDOOR) && item.getCamOutsidePrice() != null) {
                price = item.getCamOutsidePrice().longValue();
                break;
            }
        }
        return applyPriceEquipmentToAll(lstResult, price, null, null);
    }

    /**
     * Mục "L388-403" trong bảng ánh xạ: nhánh giá thiết bị thông thường (không cấu hình ON_CAM_EQUIPMENT_PRICE).
     * ⚠️ Mục E của plan: productOfferPriceRepository.getPriceEquipment khai báo thứ tự tham số
     * (productPackageId, productOfferType, productOfferId) — KHÁC thứ tự legacy (productOfferId, productOfferType, temp).
     * Gọi đúng theo tên tham số của repo hiện tại, không copy nguyên văn thứ tự legacy.
     */
    private List<ProductOfferPriceResponse> applyNormalEquipmentPrice(List<ProductOfferPriceResponse> lstResult,
                                                                       Long productPackageId, Long productOfferType,
                                                                       Long productOfferId) {
        List<ProductOfferPriceEntity> list =
                productOfferPriceRepository.getPriceEquipment(productPackageId, productOfferType, productOfferId);
        if (DataUtil.isNullOrEmpty(list)) {
            return lstResult;
        }
        if (list.size() > 1) {
            throw new BusinessException(ERROR_CODE_EQUIPMENT_MORE_THAN_ONE,
                    "Tìm thấy nhiều hơn 1 giá thiết bị cho mặt hàng: " + productOfferId);
        }
        ProductOfferPriceEntity equip = list.get(0);
        Long price = equip.getPrice() != null ? equip.getPrice().longValue() : null;
        return applyPriceEquipmentToAll(lstResult, price, equip.getProductOfferPriceId(), equip.getPriceTypeId());
    }

    /**
     * Mục "L381-386" trong bảng ánh xạ: nếu lstResult rỗng, thêm 1 bản ghi rỗng chỉ để mang giá
     * thiết bị (do ProductOfferPriceResponse là record immutable nên phải rebuild từng phần tử
     * thay vì set trực tiếp như legacy).
     */
    private List<ProductOfferPriceResponse> applyPriceEquipmentToAll(List<ProductOfferPriceResponse> lstResult,
                                                                      Long priceEquipment, Long priceEquipmentId,
                                                                      Long priceEquipmentTypeId) {
        List<ProductOfferPriceResponse> base = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(lstResult)) {
            base.add(productOfferPriceMapper.emptyWithPriceEquipment(null));
        } else {
            base.addAll(lstResult);
        }
        List<ProductOfferPriceResponse> updated = new ArrayList<>();
        for (ProductOfferPriceResponse dto : base) {
            updated.add(productOfferPriceMapper.withPriceInfo(dto, dto.productOfferName(),
                    priceEquipment, priceEquipmentId, priceEquipmentTypeId));
        }
        return updated;
    }

}