package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.client.optionset.OptionItemSnapshot;
import com.viettel.bccs.client.optionset.OptionSetResolver;
import com.viettel.bccs.productcatalog.client.MappingClient;
import com.viettel.bccs.productcatalog.client.ReasonClient;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.request.GetListStockTypeWSRequest;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferTypeStockDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingStockDTO;
import com.viettel.bccs.productcatalog.product.dto.response.StockOfferingRow;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.mapper.ProductOfferPriceMapper;
import com.viettel.bccs.productcatalog.productofferprice.service.ProductOfferPriceService;
import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.service.ProductOfferTypeService;
import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.service.TelecomServiceService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTypeWSService {

    /** Migrate từ mono: pricePolicy mặc định dùng khi tính giá trong getListStockTypeWS. */
    private static final Long DEFAULT_PRICE_POLICY = 1L;
    /** Migrate từ mono: các telecomServiceId thuộc nhóm PCCC, dùng nhánh tính giá riêng (bước 8, nhánh A). */
    private static final Long TELECOM_SERVICE_PCCC_1 = 241L;
    private static final Long TELECOM_SERVICE_PCCC_2 = 254L;
    private static final Long PRODUCT_OFFER_TYPE_HANG = 7L;
    private static final String PRODUCT_OFFER_TYPE_HANG_NAME = "Mặt hàng";

    private final TelecomServiceService telecomServiceService;
    private final ReasonClient reasonClient;
    private final MappingClient mappingClient;
    private final ProductOfferTypeService productOfferTypeService;
    private final ProductOfferingService productOfferingService;
    private final ProductOfferPriceService productOfferPriceService;
    private final ProductOfferPriceMapper productOfferPriceMapper;
    private final ObjectProvider<OptionSetResolver> optionSetResolverProvider;
    private final OptionSetValueService optionSetValueService;

    public List<ProductOfferTypeStockDTO> getListStockTypeWS(GetListStockTypeWSRequest request) {
        String actionCode = request.getActionCode();
        String regType = request.getRegType();
        String serviceType = request.getServiceType();
        String productCode = request.getProductCode();
        String viewCode = request.getViewCode();

        // Bước 1: actionCode bắt buộc (đúng như validate ở controller mono).
        if (DataUtil.isNullOrEmpty(actionCode)) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0001", "actionCode is required");
        }

        // Bước 2: regType/productCode/serviceType bắt buộc (đúng thứ tự kiểm tra ở service mono).
        if (DataUtil.isNullOrEmpty(regType)) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0002", "regType is required");
        }
        if (DataUtil.isNullOrEmpty(productCode)) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0003", "productCode is required");
        }
        if (DataUtil.isNullOrEmpty(serviceType)) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0004", "serviceType is required");
        }


        // Bước 3: dịch serviceType (alias) sang telecomServiceId.
        TelecomServiceDTO telecomServiceDTO = telecomServiceService.getTelServiceByAlias(serviceType);
        Long telecomServiceId = telecomServiceDTO != null ? telecomServiceDTO.getTelecomServiceId() : null;
        if (telecomServiceId == null || telecomServiceId == 0L) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0005",
                    "telecomServiceId not found or invalid for serviceType: " + serviceType);
        }

        // Bước 4: tìm reasonId theo regType + actionCode (mặc định "00" nếu rỗng) + telecomServiceId.

        String actionCodeForReason = DataUtil.isNullOrEmpty(actionCode) ? Const.ACTION_CODE.SUB_CONNECTION : actionCode;
        Long reasonId = reasonClient.getReasonIdByTypeAndCode(regType, actionCodeForReason, telecomServiceId);
        if (reasonId == null || reasonId == 0L) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0006", "reasonId not found or invalid for regType: " + regType);
        }

        // Bước 5: tìm saleServiceCode. Dùng actionCode GỐC (không default), đúng chữ ký legacy.
        String saleServiceCode = mappingClient.getSaleServiceCode(telecomServiceId, reasonId, productCode, actionCode);
        if (DataUtil.isNullOrEmpty(saleServiceCode)) {
            throw new BusinessException("BCCS-CATALOG-STOCKTYPE-0007", "saleServiceCode not found or invalid");
        }

        boolean didongFilter = DataUtil.safeEqual(viewCode, Const.OPTION_SET.VIEW_PRODUCT_GROUP_DIDONG);

        // Bước 6: danh sách loại hàng hoá của gói.
        List<ProductOfferTypeDTO> productOfferTypes = productOfferTypeService.findBySaleServiceCodeWithProductOffering(saleServiceCode);
        if (DataUtil.isNullOrEmpty(productOfferTypes)) {
            return List.of();
        }

        // Bước 7: danh sách mặt hàng của gói (chưa có giá).
        List<StockOfferingRow> rows;
        if (didongFilter) {
            Set<Long> allowedOfferTypeIds = resolveAllowedOfferTypeIds();
            rows = filterRowsByViewCode(
                    productOfferingService.getListStockModelBySaleServiceCode(saleServiceCode), allowedOfferTypeIds);
            if (!allowedOfferTypeIds.isEmpty()) {
                // Lọc luôn cấp loại hàng hoá: ẩn hẳn các productOfferType ngoài danh sách cho phép (kể cả khi rỗng).
                productOfferTypes = productOfferTypes.stream()
                        .filter(type -> type.getProductOfferTypeId() != null
                                && allowedOfferTypeIds.contains(type.getProductOfferTypeId()))
                        .toList();
            }
        } else {
            rows = productOfferingService.getListStockModelBySaleServiceCode(saleServiceCode);
        }

        // Bước 8 + 9: tính giá từng mặt hàng rồi nhóm theo productOfferTypeId.
        Map<Long, List<ProductOfferingStockDTO>> offeringsByType = new LinkedHashMap<>();
        for (StockOfferingRow row : rows) {
            ProductOfferingStockDTO offering = buildOffering(row, saleServiceCode);
            offeringsByType.computeIfAbsent(row.productOfferTypeId(), k -> new ArrayList<>()).add(offering);
        }

        return productOfferTypes.stream()
                .map(type -> {
                    if (PRODUCT_OFFER_TYPE_HANG.equals(type.getProductOfferTypeId())) {
                        type.setName(PRODUCT_OFFER_TYPE_HANG_NAME);
                    }
                    List<ProductOfferingStockDTO> offerings = offeringsByType.getOrDefault(type.getProductOfferTypeId(), List.of());
                    return new ProductOfferTypeStockDTO(type, offerings);
                })
                .toList();
    }


    private Set<Long> resolveAllowedOfferTypeIds() {
        return resolveFromResolver();
    }

    private Set<Long> resolveFromResolver() {
        OptionSetResolver resolver = optionSetResolverProvider.getIfAvailable();
        Set<Long> allowedOfferTypeIds = new HashSet<>();
        if (resolver == null) {
            // Resolver không tồn tại (option-set bị disable) → đọc trực tiếp từ bảng OPTION_SET_VALUE.
            return resolveFromOptionSetValue();
        }
        for (OptionItemSnapshot item : resolver.getActiveItems(Const.OPTION_SET.VIEW_PRODUCT_GROUP_DIDONG)) {
            if (item.getValue() != null && DataUtil.notNullOrEmpty(item.getValue().asText())) {
                allowedOfferTypeIds.add(DataUtil.safeToLong(item.getValue().asText()));
            }
        }
        if (allowedOfferTypeIds.isEmpty()) {
            // Cache resolver trống (không load được từ config-service) → fallback đọc DB.
            log.warn("OptionSetResolver không có dữ liệu cho [{}], fallback đọc bảng OPTION_SET_VALUE",
                    Const.OPTION_SET.VIEW_PRODUCT_GROUP_DIDONG);
            return resolveFromOptionSetValue();
        }
        return allowedOfferTypeIds;
    }

    private Set<Long> resolveFromOptionSetValue() {
        return optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VIEW_PRODUCT_GROUP_DIDONG).stream()
                .map(OptionSetValueResponse::value)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(val -> val.matches("\\d+"))
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toSet());
    }

    private ProductOfferingStockDTO buildOffering(StockOfferingRow row, String saleServiceCode) {
        List<ProductOfferPriceResponse> prices;
        if (TELECOM_SERVICE_PCCC_1.equals(row.telecomServiceId()) || TELECOM_SERVICE_PCCC_2.equals(row.telecomServiceId())) {
            List<ProductOfferPriceDTO> pcccPrices = productOfferPriceService.getPriceInServicesForPCCC(
                    null, saleServiceCode, row.productOfferTypeId(), row.productOfferingId(), DEFAULT_PRICE_POLICY);
            prices = productOfferPriceMapper.toResponseFromDto(pcccPrices);
        } else {
            List<ProductOfferPriceResponse> normalPrices = productOfferPriceService.getPriceInServices(
                    null, saleServiceCode, row.productOfferTypeId(), row.productOfferingId(), DEFAULT_PRICE_POLICY);
            prices = normalPrices != null ? normalPrices : List.of();
        }

        String productTypeName = PRODUCT_OFFER_TYPE_HANG.equals(row.productOfferTypeId())
                ? PRODUCT_OFFER_TYPE_HANG_NAME : row.typeName();

        return new ProductOfferingStockDTO(
                row.productOfferingId(),
                row.code(),
                row.name(),
                productTypeName,
                row.checkSerial(),
                row.telecomServiceId(),
                prices
        );
    }

    private List<StockOfferingRow> filterRowsByViewCode(List<StockOfferingRow> rows, Set<Long> allowedOfferTypeIds) {
        if (DataUtil.isNullOrEmpty(rows) || allowedOfferTypeIds.isEmpty()) {
            // config chưa load / không có giá trị hợp lệ → không lọc, tránh mất mặt hàng
            return rows;
        }
        return rows.stream()
                .filter(row -> row.productOfferTypeId() != null
                        && allowedOfferTypeIds.contains(row.productOfferTypeId()))
                .toList();
    }
}
