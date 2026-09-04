package com.viettel.bccs.productcatalog.productpackage.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.client.MappingClient;
import com.viettel.bccs.productcatalog.client.StaffShopClient;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.client.dto.StaffShopResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.packageoffer.service.PackageOfferService;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.service.ProdPackProductOfferTypeService;
import com.viettel.bccs.productcatalog.prodpackshop.service.ProdPackShopService;
import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.service.ProductOfferTypeService;
import com.viettel.bccs.productcatalog.productpackage.dto.response.PackageOfferDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceModelAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.mapper.ProductPackageMapper;
import com.viettel.bccs.productcatalog.productpackage.repository.ProductPackageRepository;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeDTO;
import com.viettel.bccs.productcatalog.productpackagefee.service.ProductPackageFeeService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import com.viettel.bccs.productcatalog.utils.RequestValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductPackageService {

    private final ProductPackageRepository repository;
    private final ProductPackageMapper mapper;
    private final ProductPackageFeeService productPackageFeeService;
    private final ProdPackProductOfferTypeService prodPackProductOfferTypeService;
    private final ProdPackShopService prodPackShopService;
    private final StaffShopClient staffShopClient;
    private final ProductOfferTypeService productOfferTypeService;
    private final PackageOfferService packageOfferService;
    private final OptionSetValueService optionSetValueService;
    private final MappingClient mappingClient;

    private final ProductPackageService self;

    public ProductPackageService(ProductPackageRepository repository, ProductPackageMapper mapper,
                                  ProductPackageFeeService productPackageFeeService,
                                  ProdPackProductOfferTypeService prodPackProductOfferTypeService,
                                  ProdPackShopService prodPackShopService, StaffShopClient staffShopClient,
                                  ProductOfferTypeService productOfferTypeService,
                                  PackageOfferService packageOfferService,
                                  OptionSetValueService optionSetValueService, MappingClient mappingClient,
                                  @Lazy ProductPackageService self) {
        this.repository = repository;
        this.mapper = mapper;
        this.productPackageFeeService = productPackageFeeService;
        this.prodPackProductOfferTypeService = prodPackProductOfferTypeService;
        this.prodPackShopService = prodPackShopService;
        this.staffShopClient = staffShopClient;
        this.productOfferTypeService = productOfferTypeService;
        this.packageOfferService = packageOfferService;
        this.optionSetValueService = optionSetValueService;
        this.mappingClient = mappingClient;
        this.self = self;
    }


    public List<String> findPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer packageNumber) {
        RequestValidator.requireNotBlank(excludeProdOfferType, "excludeProdOfferType", "BCCS-PRODUCT-VALIDATE-0000");
        if (packageNumber == null) {
            return findPackageCodesWithoutCount();
        }
        return findPackageCodesByProductOfferTypeCountCached(excludeProdOfferType, packageNumber);
    }

    @Cacheable(value = "productPackageCache", key = "'PKG_COUNT:' + #excludeProdOfferType + ':' + #packageNumber")
    public List<String> findPackageCodesByProductOfferTypeCountCached(String excludeProdOfferType,
        Integer packageNumber) {
        return repository.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, packageNumber);
    }

    @Cacheable(value = "productPackageCache", key = "'PKG_COUNT:ALL'")
    public List<String> findPackageCodesWithoutCount() {
        return repository.findPackageCodes();
    }

    @Cacheable(value = "productPackageCache", key = "'SALE_SVC:' + #saleServiceCode")
    public SaleServiceAdvanceDTO getSaleServicesAdvBoBySsCode(String saleServiceCode) {
        RequestValidator.requireNotBlank(saleServiceCode, "saleServiceCode", "BCCS-PRODUCT-VALIDATE-0000");
        return getSaleServicesAdvBoBySsCodeCheckStatus(saleServiceCode, true);
    }

    public ProductPackageResponse findById(Long id) {
        return repository.findById(id).map(mapper::toResponse).orElse(null);
    }

    public List<ProductPackageResponse> findByCode(String code) {
        RequestValidator.requireNotBlank(code, "code", "BCCS-PRODUCT-VALIDATE-0000");
        List<ProductPackageDTO> dtos = repository.getProductPackageExtra(code, null, true, true, true);
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(dto -> ProductPackageResponse.builder()
                .productPackageId(dto.getProductPackageId())
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .type(dto.getType())
                .effectDatetime(dto.getEffectDatetime())
                .expireDatetime(dto.getExpireDatetime())
                .createUser(dto.getCreateUser())
                .updateUser(dto.getUpdateUser())
                .version(dto.getVersion())
                .accountingId(dto.getAccountingId())
                .feeType(dto.getFeeType())
                .telecomServiceId(dto.getTelecomServiceId())
                .note(dto.getNote())
                .areaGroupId(dto.getAreaGroupId())
                .ownerShopId(dto.getOwnerShopId())
                .sapMaterialNumber(dto.getSapMaterialNumber())
                .itTelcol(dto.getItTelcol())
                .build()).toList();
    }

    public List<ProductPackageResponse> findByStatus(String status) {
        RequestValidator.requireNotBlank(status, "status", "BCCS-PRODUCT-VALIDATE-0000");
        return repository.findByStatus(status).stream().map(mapper::toResponse).toList();
    }

    public List<ProductPackageResponse> findByType(String type) {
        RequestValidator.requireNotBlank(type, "type", "BCCS-PRODUCT-VALIDATE-0000");
        return repository.findByType(type).stream().map(mapper::toResponse).toList();
    }

    public List<ProductPackageResponse> findByTelecomServiceId(Long telecomServiceId) {
        return repository.findByTelecomServiceId(telecomServiceId).stream().map(mapper::toResponse).toList();
    }

    @Cacheable(value = "productPackageCache", key = "'ACTIVE_PKG:' + #code")
    public ProductPackageDTO getActiveProductPackage(String code) {
        return repository.findActiveByCode(code).orElse(null);
    }

    @Cacheable(value = "productPackageCache", key = "'REASON_SALE_SVC:' + #reasonId")
    public List<String> findSaleServiceCodeByReasonCached(Long reasonId) {
        return mappingClient.findSaleServiceCodeByReason(reasonId);
    }

    public ProductPackageDTO getSaleServiceInfo(Long reasonId, String staffCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-PRODUCT-VALIDATE-0000");
        List<String> saleServiceCode = self.findSaleServiceCodeByReasonCached(reasonId);

        String firstValidCode = DataUtil.isNullOrEmpty(saleServiceCode) ? null : saleServiceCode.stream()
                .filter(code -> !DataUtil.isNullOrEmpty(code))
                .findFirst()
                .orElse(null);
        if (DataUtil.isNullOrEmpty(firstValidCode)) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0001",
                    "Sale service mapping not found for reason id: " + reasonId);
        }
        return getSaleServiceInfo(firstValidCode, staffCode);
    }

    public ProductPackageDTO getSaleServiceInfo(String saleServiceCode, String staffCode) {

        List<ProductPackageDTO> productPackageList = repository.getProductPackageExtra(saleServiceCode,
            Const.ProductPackageType.SALE_SERVICE, false, true, true);
        if (DataUtil.isNullOrEmpty(productPackageList)) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0002",
                "Product package not found for sale service code: " + saleServiceCode);
        }

        List<Long> offerTypeIds = optionSetValueService.findByOptionSetCode("MAT_HANG_SO").stream()
                .map(OptionSetValueResponse::value)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(val -> val.matches("\\d+"))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<ProdPackProductOfferTypeDTO> prodPackTypeList =
                prodPackProductOfferTypeService.getListByProductPackageIdAndOfferTypeIds(
                        productPackageList.get(0).getProductPackageId(), offerTypeIds);

        if (DataUtil.isNullOrEmpty(prodPackTypeList)) {
            return productPackageList.get(0);
        }


        List<Long> prodPackTypeIds = prodPackTypeList.stream()
                .map(ProdPackProductOfferTypeDTO::getProdPackTypeId)
                .collect(Collectors.toUnmodifiableList());
        Map<Long, List<Long>> prodPackTypeIdToShopIds =
                prodPackShopService.findShopIdsByProdPackTypeIds(prodPackTypeIds);

        log.debug("Found {} prodPackTypeIds mapping to shops", prodPackTypeIdToShopIds.size());

        List<Long> shopIds = prodPackTypeIdToShopIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<ShopDTO> shops = staffShopClient.findActiveByShopIds(shopIds);
        log.debug("Found {} shops for {} shopIds", shops.size(), shopIds.size());

        Map<Long, List<ShopDTO>> prodPackTypeIdToShopObject = new HashMap<>();
        Map<Long, ShopDTO> shopMap = shops.stream().collect(Collectors.toMap(ShopDTO::getShopId, s -> s));
        for (Map.Entry<Long, List<Long>> entry : prodPackTypeIdToShopIds.entrySet()) {
            List<ShopDTO> shopList = entry.getValue().stream()
                    .map(shopMap::get)
                    .filter(s -> s != null)
                    .toList();
            prodPackTypeIdToShopObject.put(entry.getKey(), shopList);
        }

        // Batch-select ProductOfferType
        List<Long> productOfferTypeIds = prodPackTypeList.stream()
                .map(ProdPackProductOfferTypeDTO::getProductOfferTypeId)
                .collect(Collectors.toUnmodifiableList());
        Map<Long, ProductOfferTypeDTO> productOfferTypeMap = productOfferTypeService.findByIds(productOfferTypeIds);

        // Batch-select PackageOffer
        Map<Long, List<PackageOfferDTO>> prodPackTypeIdToPackageOffer =
                packageOfferService.getPackageOfferByListProdPackTypeIds(prodPackTypeIds);
        for (ProdPackProductOfferTypeDTO offerTypeDTO : prodPackTypeList) {
            offerTypeDTO.setSpecShopList(prodPackTypeIdToShopObject
                    .getOrDefault(offerTypeDTO.getProdPackTypeId(), List.of()));

            ProductOfferTypeDTO productOfferTypeDTO = productOfferTypeMap.get(offerTypeDTO.getProductOfferTypeId());
            if (!DataUtil.isNullObject(productOfferTypeDTO)) {
                offerTypeDTO.setProductOfferTypeName(productOfferTypeDTO.getName());
            }

            // Set package offers from batch lookup
            List<PackageOfferDTO> packageOfferDTOList =
                    prodPackTypeIdToPackageOffer.get(offerTypeDTO.getProdPackTypeId());
            if (!DataUtil.isNullOrEmpty(packageOfferDTOList)) {
                offerTypeDTO.setPackageOfferList(packageOfferDTOList);
            }

            if (!DataUtil.isNullOrEmpty(staffCode) && DataUtil.safeEqual(offerTypeDTO.getCheckShopStock(), "1")) {
                StaffShopResponse staffShop = staffShopClient.getStaffShopFullInfo(staffCode);
                if (DataUtil.isNullObject(staffShop) || DataUtil.isNullObject(staffShop.getShop())) {
                    throw new BusinessException("BCCS-CATALOG-PACKAGE-0003", "Staff not found: " + staffCode);
                }
                offerTypeDTO.setShopId(staffShop.getShop().getShopId());
                offerTypeDTO.setShopCode(staffShop.getShop().getShopCode());
            }
        }

        productPackageList.get(0).setListProdPackType(prodPackTypeList);

        return productPackageList.get(0);
    }

    public SaleServiceAdvanceDTO getSaleServicesAdvBoBySsCodeCheckStatus(String saleServiceCode, boolean checkStatus) {
        if (DataUtil.isNullOrEmpty(saleServiceCode)) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0004", "saleServiceCode is required");
        }

        List<ProductPackageDTO> productPackageList = repository.getProductPackageExtra(
                saleServiceCode, Const.ProductPackageType.SALE_SERVICE, true, false, checkStatus);

        if (productPackageList == null || productPackageList.isEmpty()) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0005",
                    "Sale service not found for code: " + saleServiceCode);
        }

        SaleServiceAdvanceDTO saleServiceAdvanceDTO = new SaleServiceAdvanceDTO();
        saleServiceAdvanceDTO.setSuccess(true);
        saleServiceAdvanceDTO.setSaleService(productPackageList.get(0));
        saleServiceAdvanceDTO.setTlv(false);
        saleServiceAdvanceDTO.setBonus(true);

        //Lay danh sach phi cua DVBH
        List<ProductPackageFeeDTO> productPackageFeeDTOList =
                productPackageFeeService.findByProductPackageIdForPackage(productPackageList.get(0)
                        .getProductPackageId());
        saleServiceAdvanceDTO.setListSaleServicePrice(new java.util.ArrayList<>(productPackageFeeDTOList));

        //Lay danh sach loai mat hang thuoc DVBH
        List<ProdPackProductOfferTypeDTO> productOfferTypeDTOList =
                prodPackProductOfferTypeService.getByProductPackageIdAndStatus(
                        productPackageList.get(0).getProductPackageId(), Const.Status.ACTIVE);
        saleServiceAdvanceDTO.setListProductOfferType(productOfferTypeDTOList);

        List<Long> prodPackTypeIds = productOfferTypeDTOList.stream()
                .map(x -> x.getProdPackTypeId())
                .collect(Collectors.toUnmodifiableList());
        Map<Long, List<Long>> prodPackTypeIdToShopIds =
                prodPackShopService.findShopIdsByProdPackTypeIds(prodPackTypeIds);

        log.debug("Found {} prodPackTypeIds mapping to shops", prodPackTypeIdToShopIds.size());

        List<Long> shopIds = prodPackTypeIdToShopIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<ShopDTO> shops = staffShopClient.findActiveByShopIds(shopIds);
        log.debug("Found {} shops for {} shopIds", shops.size(), shopIds.size());

        Map<Long, List<ShopDTO>> prodPackTypeIdToShopObject = new HashMap<>();
        Map<Long, ShopDTO> shopMap = shops.stream().collect(Collectors.toMap(ShopDTO::getShopId, s -> s));
        for (Map.Entry<Long, List<Long>> entry : prodPackTypeIdToShopIds.entrySet()) {
            List<ShopDTO> shopList = entry.getValue().stream()
                    .map(shopMap::get)
                    .filter(s -> s != null)
                    .toList();
            prodPackTypeIdToShopObject.put(entry.getKey(), shopList);
        }
        if (!DataUtil.isNullOrEmpty(productOfferTypeDTOList)) {
            // Batch-select ProductOfferType
            List<Long> productOfferTypeIds = productOfferTypeDTOList.stream()
                    .map(ProdPackProductOfferTypeDTO::getProductOfferTypeId)
                    .collect(Collectors.toUnmodifiableList());
            Map<Long, ProductOfferTypeDTO> productOfferTypeMap = productOfferTypeService.findByIds(productOfferTypeIds);

            // Batch-select PackageOffer
            Map<Long, List<PackageOfferDTO>> prodPackTypeIdToPackageOffer =
                    packageOfferService.getPackageOfferByListProdPackTypeIds(prodPackTypeIds);

            List<SaleServiceModelAdvanceDTO> listSaleServiceModel = new ArrayList<>();

            for (ProdPackProductOfferTypeDTO offerTypeDTO : productOfferTypeDTOList) {
                offerTypeDTO.setSpecShopList(prodPackTypeIdToShopObject
                        .getOrDefault(offerTypeDTO.getProdPackTypeId(), List.of()));

                // Set productOfferTypeName from batch lookup
                ProductOfferTypeDTO productOfferTypeDTO = productOfferTypeMap.get(offerTypeDTO.getProductOfferTypeId());
                if (!DataUtil.isNullObject(productOfferTypeDTO)) {
                    offerTypeDTO.setProductOfferTypeName(productOfferTypeDTO.getName());
                }
                if (DataUtil.safeEqual(offerTypeDTO.getProductOfferTypeId(), 7L)) {
                    offerTypeDTO.setProductOfferTypeName("Mặt hàng");
                }

                // Set package offers from batch lookup
                List<PackageOfferDTO> packageOfferDTOList = 
                        prodPackTypeIdToPackageOffer.get(offerTypeDTO.getProdPackTypeId());
                if (!DataUtil.isNullOrEmpty(packageOfferDTOList)) {
                    SaleServiceModelAdvanceDTO saleServiceModelAdvanceDTO = new SaleServiceModelAdvanceDTO();
                    saleServiceModelAdvanceDTO.setSaleServiceModel(offerTypeDTO);
                    saleServiceModelAdvanceDTO.setListSaleServiceDetail(packageOfferDTOList);
                    listSaleServiceModel.add(saleServiceModelAdvanceDTO);
                }
            }
            saleServiceAdvanceDTO.setListSaleServiceModel(listSaleServiceModel);
        }

        return saleServiceAdvanceDTO;
    }

    @Cacheable(value = "productPackageCache", key = "'ACTIVE_PKG:' + #code")
    public ProductPackageResponse getActiveProductPackageResponse(String code) {
        return repository.findByCode(code)
                .filter(e -> "1".equals(e.getStatus()))
                .map(mapper::toResponse)
                .orElse(null);
    }
}