package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import com.viettel.bccs.productcatalog.productoffertype.repository.ProductOfferTypeRepository;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import com.viettel.bccs.productcatalog.telecomservice.repository.TelecomServiceRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link ProductOfferingService#getListVas(Long)}, viet lai theo dung logic
 * ProductOfferingServiceImpl.getListVas cua he mono cu (map 1:1), chi khac o nguon cau hinh
 * nhom loai tru (OptionSet thay cho file properties) va co che cache (@Cacheable thay RAM+Jedis).
 * Khong dung DB that — mock repository + 3 service cross-feature.
 */
@ExtendWith(MockitoExtension.class)
class ProductOfferingServiceTest {

    @Mock
    private ProductOfferingRepository productOfferingRepository;
    @Mock
    private ProductOfferRelationService productOfferRelationService;
    @Mock
    private ProductOfferCharUseService productOfferCharUseService;
    @Mock
    private OptionSetValueService optionSetValueService;
    @Mock
    private ProductSpecCharService productSpecCharService;
    @Mock
    private TelecomServiceRepository telecomServiceRepository;
    @Mock
    private ProductOfferTypeRepository productOfferTypeRepository;

    private ProductOfferingService service;

    @BeforeEach
    void setUp() {
        service = new ProductOfferingService(
                productOfferingRepository,
                new ProductOfferingMapper(),
                productOfferRelationService,
                productOfferCharUseService,
                optionSetValueService,
                productSpecCharService,
                new ProductSpecCharUseMapper(),
                telecomServiceRepository,
                productOfferTypeRepository);
    }

    @Test
    void getListVas_repositoryReturnsEmpty_returnsNull() {
        // Map 1:1 code cu: DataUtil.isNullOrEmpty(lst) -> return null (KHONG phai list rong)
        when(productOfferingRepository.getListVas(100L)).thenReturn(List.of());

        assertThat(service.getListVas(100L)).isNull();
    }

    @Test
    void getListVas_happyPath_enrichesRelationAndCharacteristics() {
        Long mainOfferId = 100L;
        Long vasId = 10L;

        when(productOfferingRepository.getListVas(mainOfferId))
                .thenReturn(List.of(offering(vasId, "VAS1", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId))
                .thenReturn(List.of(relation(1L, mainOfferId, vasId)));

        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId);

        assertThat(result).hasSize(1);
        ProductOfferingDTO dto = result.get(0);
        assertThat(dto.getLstProductOfferRelations()).hasSize(1);
        assertThat(dto.getLstProductOfferRelations().get(0).mainOfferId()).isEqualTo(mainOfferId);
    }

    @Test
    void getListVas_relationWithDifferentRelationType_isNotAttached() {
        // Map 1:1 code cu: mainOfferRelations lay KHONG loc theo relationTypeId, loc lai trong Java
        // theo relationTypeId == Const.RELATION_TYPE.VAS -- quan he sai type phai bi bo qua.
        Long mainOfferId = 100L;
        Long vasId = 10L;

        when(productOfferingRepository.getListVas(mainOfferId))
                .thenReturn(List.of(offering(vasId, "VAS1", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE)));

        ProductOfferRelationResponse wrongTypeRelation = new ProductOfferRelationResponse(
                1L, 999L, mainOfferId, vasId, Const.STATUS.ACTIVE, "system", null, "system", null, null, null, null, null);
        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of(wrongTypeRelation));

        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLstProductOfferRelations()).isNull();
    }

    @Test
    void getListVas_sameExclusiveGroup_shareTypeIndex_andUngroupedVasEachGetOwnIndex() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId)).thenReturn(List.of(
                offering(10L, "GPRS0", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE),
                offering(11L, "GPRS5", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE),
                offering(12L, "STANDALONE_A", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE),
                offering(13L, "STANDALONE_B", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of(
                groupRow("PRE_GPRS", "GPRS0"),
                groupRow("PRE_GPRS", "GPRS5")
        ));

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId);

        Integer indexA = typeIndexOf(result, 10L);
        Integer indexB = typeIndexOf(result, 11L);
        Integer indexC = typeIndexOf(result, 12L);
        Integer indexD = typeIndexOf(result, 13L);

        assertThat(indexA).isEqualTo(indexB); // cung nhom PRE_GPRS -> loai tru lan nhau
        assertThat(indexC).isNotEqualTo(indexA);
        assertThat(indexD).isNotEqualTo(indexA);
        assertThat(indexC).isNotEqualTo(indexD); // 2 VAS khong thuoc nhom nao -> moi ma 1 nhom rieng, KHAC nhau
    }

    /**
     * Regression: mot ma VAS (giong BBMAIL/BBCHT trong config cu) co the la ung vien cua CA HAI
     * nhom PRE_BB va POS_BB cung luc (2 ban ghi khac product_offering_id, cung code, khac subType).
     * Vi PRE_BB/POS_BB duoc doc thanh 2 List<String> DOC LAP (dung getVasExcludeGroup rieng cho
     * tung nhom, giong het code cu goi getVasExclude rieng cho tung nhom) roi kiem tra qua chuoi
     * if-else-if + dieu kien subType, khong con bi gop chung vao 1 map nhu thiet ke truoc do nen
     * khong the tai hien lai bug "1 ma chi giu duoc 1 nhom".
     */
    @Test
    void getListVas_sameCodeInBothPreAndPostBbGroup_disambiguatedBySubType() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId)).thenReturn(List.of(
                offering(10L, "BBMAIL", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE),
                offering(11L, "BBMAIL", Const.STATUS.ACTIVE, Const.SUB_TYPE.POST)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of(
                groupRow("PRE_BB", "BBMAIL"),
                groupRow("POS_BB", "BBMAIL")
        ));

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId);

        Integer indexPre = typeIndexOf(result, 10L);
        Integer indexPost = typeIndexOf(result, 11L);

        assertThat(indexPre).isNotNull();
        assertThat(indexPost).isNotNull();
        assertThat(indexPre).isNotEqualTo(indexPost);
    }

    private Integer typeIndexOf(List<ProductOfferingDTO> result, Long productOfferingId) {
        return result.stream()
                .filter(dto -> productOfferingId.equals(dto.getProductOfferingId()))
                .findFirst()
                .map(ProductOfferingDTO::getTypeIndex)
                .orElseThrow();
    }

    private ProductOfferRelationResponse relation(Long relationId, Long mainOfferId, Long relationOfferId) {
        return new ProductOfferRelationResponse(relationId, Const.RELATION_TYPE.VAS, mainOfferId, relationOfferId,
                Const.STATUS.ACTIVE, "system", null, "system", null, null, null, null, null);
    }

    private ProductOfferingEntity offering(Long id, String code, String status, String subType) {
        return ProductOfferingEntity.builder()
                .productOfferingId(id)
                .code(code)
                .name(code)
                .status(status)
                .productOfferTypeId(Const.PRODUCT_OFFER_TYPE.VAS)
                .subType(subType)
                .build();
    }

    private OptionSetValueResponse groupRow(String groupName, String vasCode) {
        return new OptionSetValueResponse(null, null, null, groupName, vasCode, Const.STATUS.ACTIVE, null, null, null, null, null, null);
    }

    @Test
    void getByTelServiceIdAndSubTypeAndProductOfferTypeId_telServiceIdNotExist_throwsBusinessException() {
        when(telecomServiceRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByTelServiceIdAndSubTypeAndProductOfferTypeId(1L, Const.SUB_TYPE.POST, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("BCCS-CATALOG-PRODUCT-0006");
    }

    @Test
    void getByTelServiceIdAndSubTypeAndProductOfferTypeId_subTypeNotInKnownSet_throwsBusinessException() {
        when(telecomServiceRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.getByTelServiceIdAndSubTypeAndProductOfferTypeId(1L, "9", null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("BCCS-CATALOG-PRODUCT-0006");
    }

    @Test
    void getByTelServiceIdAndSubTypeAndProductOfferTypeId_productOfferTypeIdNotExist_throwsBusinessException() {
        when(telecomServiceRepository.existsById(1L)).thenReturn(true);
        when(productOfferTypeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByTelServiceIdAndSubTypeAndProductOfferTypeId(1L, Const.SUB_TYPE.POST, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("BCCS-CATALOG-PRODUCT-0006");
    }

    @Test
    void getByTelServiceIdAndSubTypeAndProductOfferTypeId_validInput_returnsActiveOfferingsOnly() {
        when(telecomServiceRepository.existsById(1L)).thenReturn(true);
        when(productOfferingRepository.findByTelecomSubTypeOfferTypeCheckProductStatus(1L, Const.SUB_TYPE.POST, null, true))
                .thenReturn(List.of(offering(100L, "CODE_A", Const.STATUS.ACTIVE, Const.SUB_TYPE.POST)));

        List<ProductOfferingDTO> result = service.getByTelServiceIdAndSubTypeAndProductOfferTypeId(1L, Const.SUB_TYPE.POST, null);

        assertThat(result).extracting(ProductOfferingDTO::getCode).containsExactly("CODE_A");
    }

    @Test
    void getByTelServiceIdAndSubTypeAndProductOfferTypeId_productOfferTypeIdProvidedAndValid_delegatesWithFilter() {
        when(telecomServiceRepository.existsById(1L)).thenReturn(true);
        when(productOfferTypeRepository.existsById(5L)).thenReturn(true);
        when(productOfferingRepository.findByTelecomSubTypeOfferTypeCheckProductStatus(1L, Const.SUB_TYPE.PRE, 5L, true))
                .thenReturn(List.of(offering(200L, "CODE_B", Const.STATUS.ACTIVE, Const.SUB_TYPE.PRE)));

        List<ProductOfferingDTO> result = service.getByTelServiceIdAndSubTypeAndProductOfferTypeId(1L, Const.SUB_TYPE.PRE, 5L);

        assertThat(result).extracting(ProductOfferingDTO::getCode).containsExactly("CODE_B");
    }
}
