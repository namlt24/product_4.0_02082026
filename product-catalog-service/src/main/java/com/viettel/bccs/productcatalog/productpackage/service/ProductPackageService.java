package com.viettel.bccs.productcatalog.productpackage.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.client.MappingClient;
import com.viettel.bccs.productcatalog.client.StaffShopClient;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.client.dto.StaffShopResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.service.ProductOfferTypeService;
import com.viettel.bccs.productcatalog.productpackage.dto.response.*;
import com.viettel.bccs.productcatalog.productpackage.entity.ProductPackageEntity;
import com.viettel.bccs.productcatalog.productpackage.mapper.ProductPackageMapper;
import com.viettel.bccs.productcatalog.productpackage.repository.ProductPackageRepository;
import com.viettel.bccs.productcatalog.packageoffer.service.PackageOfferService;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeDTO;
import com.viettel.bccs.productcatalog.productpackagefee.service.ProductPackageFeeService;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.service.ProdPackProductOfferTypeService;
import com.viettel.bccs.productcatalog.prodpackshop.service.ProdPackShopService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import com.viettel.bccs.productcatalog.utils.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    // Self-inject qua proxy: findSaleServiceCodeByReasonCached được gọi nội bộ từ getSaleServiceInfo(Long)
    // trong cùng class -> nếu gọi qua "this" (self-invocation) sẽ bỏ qua Spring AOP proxy, khiến
    // @Cacheable ở method đó vô tác dụng. @Lazy để tránh vòng lặp khởi tạo bean khi tự inject chính nó.
    private final ProductPackageService self;

    public ProductPackageService(ProductPackageRepository repository, ProductPackageMapper mapper,
                                  ProductPackageFeeService productPackageFeeService,
                                  ProdPackProductOfferTypeService prodPackProductOfferTypeService,
                                  ProdPackShopService prodPackShopService, StaffShopClient staffShopClient,
                                  ProductOfferTypeService productOfferTypeService, PackageOfferService packageOfferService,
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


    public List<String> findPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer pNumber) {
        if (pNumber == null) {
            return findPackageCodesWithoutCount();
        }
        return findPackageCodesByProductOfferTypeCountCached(excludeProdOfferType, pNumber);
    }

    @Cacheable(value = "productPackageCache", key = "'PKG_COUNT:' + #excludeProdOfferType + ':' + #pNumber")
    public List<String> findPackageCodesByProductOfferTypeCountCached(String excludeProdOfferType, Integer pNumber) {
        return repository.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber);
    }

    @Cacheable(value = "productPackageCache", key = "'PKG_COUNT:ALL'")
    public List<String> findPackageCodesWithoutCount() {
        return repository.findPackageCodes();
    }

    @Cacheable(value = "productPackageCache", key = "'SALE_SVC:' + #saleServiceCode")
    public SaleServiceAdvanceDTO getSaleServicesAdvBOBySSCode(String saleServiceCode) {
        return getSaleServicesAdvBOBySSCodeCheckStatus(saleServiceCode, true);
    }

    public ProductPackageResponse findById(Long id) {
        return repository.findById(id).map(mapper::toResponse).orElse(null);
    }

    public List<ProductPackageResponse> findByCode(String code) {
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
        return repository.findByStatus(status).stream().map(mapper::toResponse).toList();
    }

    public List<ProductPackageResponse> findByType(String type) {
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
        List<String> saleServiceCode = self.findSaleServiceCodeByReasonCached(reasonId);

        String firstValidCode = DataUtil.isNullOrEmpty(saleServiceCode) ? null : saleServiceCode.stream()
                .filter(code -> !DataUtil.isNullOrEmpty(code))
                .findFirst()
                .orElse(null);
        if (DataUtil.isNullOrEmpty(firstValidCode)) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0001", "Sale service mapping not found for reason id: " + reasonId);
        }
        return getSaleServiceInfo(firstValidCode, staffCode);
    }

    public ProductPackageDTO getSaleServiceInfo(String saleServiceCode, String staffCode) {

        List<ProductPackageDTO> productPackageDTOs = repository.getProductPackageExtra(saleServiceCode, Const.PRODUCT_PACKAGE_TYPE.SALE_SERVICE, false, true, true);
        if (DataUtil.isNullOrEmpty(productPackageDTOs)) {
            throw new BusinessException("BCCS-CATALOG-PACKAGE-0002", "Product package not found for sale service code: " + saleServiceCode);
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
                        productPackageDTOs.get(0).getProductPackageId(), offerTypeIds);

        if (DataUtil.isNullOrEmpty(prodPackTypeList)) {
            return productPackageDTOs.get(0);
        }


        List<Long> prodPackTypeIds = prodPackTypeList.stream().map(ProdPackProductOfferTypeDTO::getProdPackTypeId).collect(Collectors.toUnmodifiableList());
        Map<Long, List<Long>> prodPackTypeIdToShopIds = prodPackShopService.findShopIdsByProdPackTypeIds(prodPackTypeIds);

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
        Map<Long, List<PackageOfferDTO>> prodPackTypeIdToPackageOffer = packageOfferService.getPackageOfferByListProdPackTypeIds(prodPackTypeIds);

        for (ProdPackProductOfferTypeDTO offerTypeDTO : prodPackTypeList) {
            offerTypeDTO.setSpecShopList(prodPackTypeIdToShopObject.getOrDefault(offerTypeDTO.getProdPackTypeId(), List.of()));

            ProductOfferTypeDTO productOfferTypeDTO = productOfferTypeMap.get(offerTypeDTO.getProductOfferTypeId());
            if (!DataUtil.isNullObject(productOfferTypeDTO)) {
                offerTypeDTO.setProductOfferTypeName(productOfferTypeDTO.getName());
            }

            // Set package offers from batch lookup
            List<PackageOfferDTO> packageOfferDTOList = prodPackTypeIdToPackageOffer.get(offerTypeDTO.getProdPackTypeId());
            if (!DataUtil.isNullOrEmpty(packageOfferDTOList)) {
                offerTypeDTO.setPackageOfferDTOs(packageOfferDTOList);
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

        productPackageDTOs.get(0).setListProdPackType(prodPackTypeList);

        return productPackageDTOs.get(0);
    }

    public SaleServiceAdvanceDTO getSaleServicesAdvBOBySSCodeCheckStatus(String saleServiceCode, boolean checkStatus) {
        SaleServiceAdvanceDTO saleServiceAdvanceDTO = new SaleServiceAdvanceDTO();
        saleServiceAdvanceDTO.setSuccess(true);
        try {
            if (DataUtil.isNullOrEmpty(saleServiceCode)) {
                saleServiceAdvanceDTO.setSuccess(false);
                saleServiceAdvanceDTO.setErrorCode(ErrorCode.ERROR_STANDARD.ERROR_VALIDATE_INPUT);
                saleServiceAdvanceDTO.setKeyMsg("product.package.saleService.requireMsg.code");
                return saleServiceAdvanceDTO;
            }

            List<ProductPackageDTO> productPackageDTOs = repository.getProductPackageExtra(
                    saleServiceCode, Const.PRODUCT_PACKAGE_TYPE.SALE_SERVICE, true, false, checkStatus);

            if (productPackageDTOs == null || productPackageDTOs.isEmpty()) {
                saleServiceAdvanceDTO.setSuccess(false);
                saleServiceAdvanceDTO.setErrorCode(ErrorCode.ERROR_STANDARD.ERROR_VALIDATE_INPUT);
                saleServiceAdvanceDTO.setKeyMsg("product.package.saleService.notExist");
                return saleServiceAdvanceDTO;
            }

            saleServiceAdvanceDTO.setSaleService(productPackageDTOs.get(0));
            saleServiceAdvanceDTO.setTLV(false);
            saleServiceAdvanceDTO.setBonus(true);

            //Lay danh sach phi cua DVBH
            List<ProductPackageFeeDTO> productPackageFeeDTOList = productPackageFeeService.findByProductPackageIdForPackage(productPackageDTOs.get(0).getProductPackageId());
            saleServiceAdvanceDTO.setListSaleServicePrice(new java.util.ArrayList<>(productPackageFeeDTOList));

            //Lay danh sach loai mat hang thuoc DVBH
            List<ProdPackProductOfferTypeDTO> productOfferTypeDTOList = prodPackProductOfferTypeService.getByProductPackageIdAndStatus(
                    productPackageDTOs.get(0).getProductPackageId(), Const.STATUS.ACTIVE);
            saleServiceAdvanceDTO.setListProductOfferType(productOfferTypeDTOList);

            List<Long> prodPackTypeIds = productOfferTypeDTOList.stream().map(x -> x.getProdPackTypeId()).collect(Collectors.toUnmodifiableList());
            Map<Long, List<Long>> prodPackTypeIdToShopIds = prodPackShopService.findShopIdsByProdPackTypeIds(prodPackTypeIds);

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
                Map<Long, List<PackageOfferDTO>> prodPackTypeIdToPackageOffer = packageOfferService.getPackageOfferByListProdPackTypeIds(prodPackTypeIds);

                List<SaleServiceModelAdvanceDTO> listSaleServiceModel = new ArrayList<>();

                for (ProdPackProductOfferTypeDTO offerTypeDTO : productOfferTypeDTOList) {
                    offerTypeDTO.setSpecShopList(prodPackTypeIdToShopObject.getOrDefault(offerTypeDTO.getProdPackTypeId(), List.of()));

                    // Set productOfferTypeName from batch lookup
                    ProductOfferTypeDTO productOfferTypeDTO = productOfferTypeMap.get(offerTypeDTO.getProductOfferTypeId());
                    if (!DataUtil.isNullObject(productOfferTypeDTO)) {
                        offerTypeDTO.setProductOfferTypeName(productOfferTypeDTO.getName());
                    }
                    if (DataUtil.safeEqual(offerTypeDTO.getProductOfferTypeId(), 7L)) {
                        offerTypeDTO.setProductOfferTypeName("Mặt hàng");
                    }

                    // Set package offers from batch lookup
                    List<PackageOfferDTO> packageOfferDTOList = prodPackTypeIdToPackageOffer.get(offerTypeDTO.getProdPackTypeId());
                    if (!DataUtil.isNullOrEmpty(packageOfferDTOList)) {
                        SaleServiceModelAdvanceDTO saleServiceModelAdvanceDTO = new SaleServiceModelAdvanceDTO();
                        saleServiceModelAdvanceDTO.setSaleServiceModel(offerTypeDTO);
                        saleServiceModelAdvanceDTO.setListSaleServiceDetail(packageOfferDTOList);
                        listSaleServiceModel.add(saleServiceModelAdvanceDTO);
                    }
                }
                saleServiceAdvanceDTO.setListSaleServiceModel(listSaleServiceModel);
            }

        } catch (Exception e) {
            saleServiceAdvanceDTO.setSuccess(false);
            saleServiceAdvanceDTO.setErrorCode(ErrorCode.ERROR_NOT_DEFINE);
            saleServiceAdvanceDTO.setKeyMsg("common.error.happened");
            log.error("Exception in getSaleServicesAdvBOBySSCodeCheckStatus", e);
        }
        return saleServiceAdvanceDTO;
    }
}