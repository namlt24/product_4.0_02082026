package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import com.viettel.bccs.productcatalog.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link ProductOfferingService#getListVas(Long, Integer)}, viet lai theo dung logic
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
                new ProductSpecCharUseMapper());
    }

    @Test
    void getListVas_repositoryReturnsEmpty_returnsNull() {
        // Map 1:1 code cu: DataUtil.isNullOrEmpty(lst) -> return null (KHONG phai list rong)
        when(productOfferingRepository.getListVas(100L, null)).thenReturn(List.of());

        assertThat(service.getListVas(100L, null)).isNull();
    }

    @Test
    void getListVas_happyPath_enrichesRelationAndCharacteristics() {
        Long mainOfferId = 100L;
        Long vasId = 10L;

        when(productOfferingRepository.getListVas(mainOfferId, null))
                .thenReturn(List.of(offering(vasId, "VAS1", Const.Status.ACTIVE, Const.SubType.PRE)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId))
                .thenReturn(List.of(relation(1L, mainOfferId, vasId)));

        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId, null);

        assertThat(result).hasSize(1);
        ProductOfferingDTO dto = result.get(0);
        assertThat(dto.getLstProductOfferRelations()).hasSize(1);
        assertThat(dto.getLstProductOfferRelations().get(0).mainOfferId()).isEqualTo(mainOfferId);
    }

    @Test
    void getListVas_relationWithDifferentRelationType_isNotAttached() {
        // Map 1:1 code cu: mainOfferRelations lay KHONG loc theo relationTypeId, loc lai trong Java
        // theo relationTypeId == Const.RelationType.VAS -- quan he sai type phai bi bo qua.
        Long mainOfferId = 100L;
        Long vasId = 10L;

        when(productOfferingRepository.getListVas(mainOfferId, null))
                .thenReturn(List.of(offering(vasId, "VAS1", Const.Status.ACTIVE, Const.SubType.PRE)));

        ProductOfferRelationResponse wrongTypeRelation = new ProductOfferRelationResponse(
                1L, 999L, mainOfferId, vasId, Const.Status.ACTIVE, "system", null, "system", null, null, null, null, null);
        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of(wrongTypeRelation));

        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLstProductOfferRelations()).isNull();
    }

    @Test
    void getListVas_sameExclusiveGroup_shareTypeIndex_andUngroupedVasEachGetOwnIndex() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId, null)).thenReturn(List.of(
                offering(10L, "GPRS0", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(11L, "GPRS5", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(12L, "STANDALONE_A", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(13L, "STANDALONE_B", Const.Status.ACTIVE, Const.SubType.PRE)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of(
                groupRow("PRE_GPRS", "GPRS0"),
                groupRow("PRE_GPRS", "GPRS5")
        ));

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId, null);

        Integer indexA = typeIndexOf(result, 10L);
        Integer indexB = typeIndexOf(result, 11L);
        Integer indexC = typeIndexOf(result, 12L);
        Integer indexD = typeIndexOf(result, 13L);

        assertThat(indexA).isEqualTo(indexB); // cung nhom PreGprs -> loai tru lan nhau
        assertThat(indexC).isNotEqualTo(indexA);
        assertThat(indexD).isNotEqualTo(indexA);
        assertThat(indexC).isNotEqualTo(indexD); // 2 VAS khong thuoc nhom nao -> moi ma 1 nhom rieng, KHAC nhau
    }

    /**
     * Regression: mot ma VAS (giong BBMAIL/BBCHT trong config cu) co the la ung vien cua CA HAI
     * nhom PreBb va PosBb cung luc (2 ban ghi khac product_offering_id, cung code, khac subType).
     * Vi PreBb/PosBb duoc doc thanh 2 List<String> DOC LAP (dung getVasExcludeGroup rieng cho
     * tung nhom, giong het code cu goi getVasExclude rieng cho tung nhom) roi kiem tra qua chuoi
     * if-else-if + dieu kien subType, khong con bi gop chung vao 1 map nhu thiet ke truoc do nen
     * khong the tai hien lai bug "1 ma chi giu duoc 1 nhom".
     */
    @Test
    void getListVas_sameCodeInBothPreAndPostBbGroup_disambiguatedBySubType() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId, null)).thenReturn(List.of(
                offering(10L, "BBMAIL", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(11L, "BBMAIL", Const.Status.ACTIVE, Const.SubType.POST)));

        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of(
                groupRow("PRE_BB", "BBMAIL"),
                groupRow("POS_BB", "BBMAIL")
        ));

        List<ProductOfferingDTO> result = service.getListVas(mainOfferId, null);

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
        return new ProductOfferRelationResponse(relationId, Const.RelationType.VAS, mainOfferId, relationOfferId,
                Const.Status.ACTIVE, "system", null, "system", null, null, null, null, null);
    }

    private ProductOfferingEntity offering(Long id, String code, String status, String subType) {
        return ProductOfferingEntity.builder()
                .productOfferingId(id)
                .code(code)
                .name(code)
                .status(status)
                .productOfferTypeId(Const.ProductOfferType.VAS)
                .subType(subType)
                .build();
    }

    private OptionSetValueResponse groupRow(String groupName, String vasCode) {
        return new OptionSetValueResponse(null, null, null, groupName, vasCode, Const.Status.ACTIVE, null, null, null, null, null, null);
    }

    @Test
    void getListVas_multipleOffers_batchesSpecCharLookupInOneCall() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId, null)).thenReturn(List.of(
                offering(10L, "VAS_A", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(11L, "VAS_B", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(12L, "VAS_C", Const.Status.ACTIVE, Const.SubType.PRE)));
        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        service.getListVas(mainOfferId, null);

        // Fix N+1: truoc day goi 1 lan/VAS (3 lan cho 3 phan tu), nay phai gom het thanh 1 lan
        // duy nhat truoc vong lap, dung dung kha nang batch da co san cua method nay.
        verify(productOfferCharUseService, times(1)).getProductSpecCharByOfferingIds(anyList());
    }

    @Test
    void getListVas_multipleOffers_fetchesExclusiveGroupOnce() {
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId, null)).thenReturn(List.of(
                offering(10L, "VAS_A", Const.Status.ACTIVE, Const.SubType.PRE),
                offering(11L, "VAS_B", Const.Status.ACTIVE, Const.SubType.PRE)));
        when(productOfferRelationService.findByMainOfferId(mainOfferId)).thenReturn(List.of());
        when(productOfferCharUseService.getProductSpecCharByOfferingIds(anyList())).thenReturn(Map.of());
        when(optionSetValueService.findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP)).thenReturn(List.of());

        service.getListVas(mainOfferId, null);

        // Truoc day getVasExcludeGroup tu query lai findByOptionSetCode cho MOI 1 trong 8 nhom
        // loai tru (PreGprs, PosGprs, ...) -> 8 lan goi giong het nhau. Phai gom ve 1 lan.
        verify(optionSetValueService, times(1)).findByOptionSetCode(Const.OptionSet.VAS_EXCLUSIVE_GROUP);
    }

    @Test
    void getListVas_typeProvided_passesThroughToRepository() {
        // Loc theo type nam o tang SQL (khong test duoc bang Mockito) -- unit test nay chi xac
        // nhan tham so type duoc truyen xuyen suot dung xuong repository, khong bi mat/doi gia tri.
        Long mainOfferId = 100L;
        when(productOfferingRepository.getListVas(mainOfferId, 1)).thenReturn(List.of());

        assertThat(service.getListVas(mainOfferId, 1)).isNull();

        verify(productOfferingRepository, times(1)).getListVas(mainOfferId, 1);
    }
}
