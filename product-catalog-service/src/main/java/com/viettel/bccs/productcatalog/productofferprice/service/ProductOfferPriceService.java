package com.viettel.bccs.productcatalog.productofferprice.service;

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
    private static final String ERROR_CODE_EQUIPMENT_MORE_THAN_ONE = "PRICE-001";
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

}