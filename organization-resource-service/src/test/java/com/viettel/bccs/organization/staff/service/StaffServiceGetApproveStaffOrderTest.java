package com.viettel.bccs.organization.staff.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.common.error.exception.ValidationException;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import com.viettel.bccs.organization.client.OptionSetClient;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import com.viettel.bccs.organization.shop.dto.response.ShopResponse;
import com.viettel.bccs.organization.shop.mapper.ShopMapper;
import com.viettel.bccs.organization.shop.repository.ShopRepository;
import com.viettel.bccs.organization.shop.service.ShopService;
import com.viettel.bccs.organization.staff.dto.response.StaffSummaryDTO;
import com.viettel.bccs.organization.staff.entity.StaffEntity;
import com.viettel.bccs.organization.staff.mapper.StaffMapper;
import com.viettel.bccs.organization.staff.repository.StaffRepository;
import com.viettel.bccs.organization.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test cho StaffService#getApproveStaffOrder theo note yêu cầu B1-B5.
 * Dùng StaffMapper thật (không mock) để cover cả bước map sang StaffSummaryDTO,
 * mock các dependency còn lại.
 */
@ExtendWith(MockitoExtension.class)
class StaffServiceGetApproveStaffOrderTest {

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private ShopService shopService;
    @Mock
    private OptionSetClient optionSetClient;
    @Mock
    private CustTypeService custTypeService;
    @Mock
    private ChannelTypeService channelTypeService;

    private StaffMapper staffMapper;
    private StaffService staffService;

    @BeforeEach
    void setUp() {
        staffMapper = new StaffMapper();
        staffService = new StaffService(staffRepository, shopRepository, staffMapper,
                new ShopMapper(), shopService, optionSetClient, custTypeService, channelTypeService);
    }

    // ---------- B1: validate input ----------

    @Test
    void blankStaffCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> staffService.getApproveStaffOrder("   "));
    }

    @Test
    void nullStaffCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> staffService.getApproveStaffOrder(null));
    }

    // ---------- B2: staff not found ----------

    @Test
    void staffNotFound_throwsBusinessException() {
        when(staffRepository.findByStaffCodeAndStatus(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> staffService.getApproveStaffOrder("NV_NOT_EXIST"));
    }

    // ---------- B3: staff has own staff_owner_id ----------

    @Test
    void staffOwnerIdPresent_returnsOwnerStaff() {
        StaffEntity staff = staffEntity(100L, "NV_001", 50L, 900L);
        StaffEntity owner = staffEntity(900L, "NV_OWNER_900", 60L, null);
        when(staffRepository.findByStaffCodeAndStatus("NV_001", Const.Status.ACTIVE)).thenReturn(Optional.of(staff));
        when(staffRepository.findByStaffIdAndStatus(900L, Const.Status.ACTIVE)).thenReturn(Optional.of(owner));

        StaffSummaryDTO result = staffService.getApproveStaffOrder("NV_001");

        assertEquals(900L, result.getStaffId());
        assertEquals("NV_OWNER_900", result.getStaffCode());
        assertEquals("NV_OWNER_900", result.getName());
    }

    // ---------- B4: no staff owner, but shop has staff_owner_id ----------

    @Test
    void noStaffOwner_shopHasStaffOwner_returnsShopManager() {
        StaffEntity staff = staffEntity(100L, "NV_001", 50L, null); // shopId=50, ko staffOwnerId
        StaffEntity shopManager = staffEntity(700L, "NV_SHOP_MGR", 50L, null);
        ShopResponse shop = shopResponse(50L, null, "NV_SHOP_MGR", 7L);

        when(staffRepository.findByStaffCodeAndStatus("NV_001", Const.Status.ACTIVE)).thenReturn(Optional.of(staff));
        when(shopRepository.findByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(Optional.of(shopEntity(50L, null, 700L)));
        when(staffRepository.findByStaffIdAndStatus(700L, Const.Status.ACTIVE)).thenReturn(Optional.of(shopManager));

        StaffSummaryDTO result = staffService.getApproveStaffOrder("NV_001");

        assertEquals(700L, result.getStaffId());
        assertEquals("NV_SHOP_MGR", result.getStaffCode());
        assertEquals("NV_SHOP_MGR", result.getName());
    }

    // ---------- B5: climb to level-3 shop, pick random active staff ----------

    @Test
    void noOwners_level3ShopHasStaff_returnsRandomFromLevel3() {
        StaffEntity staff = staffEntity(100L, "NV_001", 50L, null);
        // 50 là shop cấp 3 trong shop_path "_1_10_50"
        ShopResponse shop = shopResponse(50L, "_1_10_50", null, null);

        StaffEntity lvl3a = staffEntity(201L, "NV_L3_A", 50L, null);
        StaffEntity lvl3b = staffEntity(202L, "NV_L3_B", 50L, null);

        when(staffRepository.findByStaffCodeAndStatus("NV_001", Const.Status.ACTIVE)).thenReturn(Optional.of(staff));
        when(shopRepository.findByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(Optional.of(shopEntity(50L, "_1_10_50", null)));
        when(staffRepository.findAllByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(List.of(lvl3a, lvl3b));

        StaffSummaryDTO result = staffService.getApproveStaffOrder("NV_001");

        // random từ {NV_L3_A, NV_L3_B} — phải trả về 1 trong 2
        org.junit.jupiter.api.Assertions.assertTrue(
                result.getStaffId() == 201L || result.getStaffId() == 202L,
                "Phải trả về staff random thuộc shop cấp 3 (201 hoặc 202), thực tế: " + result.getStaffId());
    }

    @Test
    void noOwners_level3ShopEmpty_returnsNull() {
        StaffEntity staff = staffEntity(100L, "NV_001", 50L, null);
        ShopResponse shop = shopResponse(50L, "_1_10_50", null, null);

        when(staffRepository.findByStaffCodeAndStatus("NV_001", Const.Status.ACTIVE)).thenReturn(Optional.of(staff));
        when(shopRepository.findByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(Optional.of(shopEntity(50L, "_1_10_50", null)));
        when(staffRepository.findAllByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(List.of());

        assertNull(staffService.getApproveStaffOrder("NV_001"));
    }

    @Test
    void shopPathTooShort_returnsNull() {
        StaffEntity staff = staffEntity(100L, "NV_001", 50L, null);
        ShopResponse shop = shopResponse(50L, "_1_50", null, null); // chỉ 2 mức, không có cấp 3

        when(staffRepository.findByStaffCodeAndStatus("NV_001", Const.Status.ACTIVE)).thenReturn(Optional.of(staff));
        when(shopRepository.findByShopIdAndStatus(50L, Const.Status.ACTIVE)).thenReturn(Optional.of(shopEntity(50L, "_1_50", null)));

        assertNull(staffService.getApproveStaffOrder("NV_001"));
    }

    // ---------- helpers ----------

    private StaffEntity staffEntity(Long staffId, String staffCode, Long shopId, Long staffOwnerId) {
        StaffEntity e = new StaffEntity();
        e.setStaffId(staffId);
        e.setStaffCode(staffCode);
        e.setName(staffCode);
        e.setShopId(shopId);
        e.setStaffOwnerId(staffOwnerId);
        return e;
    }

    private com.viettel.bccs.organization.shop.entity.ShopEntity shopEntity(Long shopId, String shopPath, Long staffOwnerId) {
        com.viettel.bccs.organization.shop.entity.ShopEntity e =
                new com.viettel.bccs.organization.shop.entity.ShopEntity();
        e.setShopId(shopId);
        e.setShopPath(shopPath);
        e.setStaffOwnerId(staffOwnerId);
        return e;
    }

    private ShopResponse shopResponse(Long shopId, String shopPath, String shopCode, Long staffOwnerId) {
        return ShopResponse.builder()
                .shopId(shopId)
                .shopPath(shopPath)
                .shopCode(shopCode)
                .staffOwnerId(staffOwnerId)
                .build();
    }

}
