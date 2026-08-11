package com.viettel.bccs.policy.common.helper;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.StaffShopClient;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.client.dto.StaffResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ShopResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link StaffResolveHelper}, bao gồm cả overload cũ {@code resolveStaffDTO(String)}
 * (không đổi hành vi) và overload mới {@code resolveStaffDTO(Long)} vừa thêm.
 */
@ExtendWith(MockitoExtension.class)
class StaffResolveHelperTest {

    @Mock
    private StaffShopClient staffShopClient;

    private StaffResolveHelper helper;

    @BeforeEach
    void setUp() {
        helper = new StaffResolveHelper(staffShopClient);
    }

    private static StaffResponse fullStaffResponse() {
        ShopResponse shop = ShopResponse.builder()
                .shopCode("SHOP001")
                .province("HN")
                .district("BD")
                .precinct("P1")
                .channelTypeId(7L)
                .build();
        return StaffResponse.builder()
                .staffId(123L)
                .staffCode("NV_001")
                .shopId(456L)
                .shop(shop)
                .build();
    }

    @Test
    void resolveStaffDTO_byStaffCode_mapsShopDerivedFields() {
        when(staffShopClient.getStaffShopFullInfo("NV_001")).thenReturn(fullStaffResponse());

        StaffDTO result = helper.resolveStaffDTO("NV_001");

        assertThat(result.getStaffId()).isEqualTo(123L);
        assertThat(result.getShopCode()).isEqualTo("SHOP001");
        assertThat(result.getShopProvince()).isEqualTo("HN");
        assertThat(result.getShopDistrict()).isEqualTo("BD");
        assertThat(result.getShopPrecinct()).isEqualTo("P1");
        assertThat(result.getShopChanelTypeId()).isEqualTo(7L);
    }

    @Test
    void resolveStaffDTO_byStaffCode_nullResponse_throwsBusinessException() {
        when(staffShopClient.getStaffShopFullInfo("UNKNOWN")).thenReturn(null);

        BusinessException ex = catchThrowableOfType(
                () -> helper.resolveStaffDTO("UNKNOWN"), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo("BCCS-POLICY-MAPACTIVE-0006");
    }

    @Test
    void resolveStaffDTO_byStaffId_mapsShopDerivedFields_sameAsStaffCodeOverload() {
        when(staffShopClient.getStaffShopFullInfoByStaffId(123L)).thenReturn(fullStaffResponse());

        StaffDTO result = helper.resolveStaffDTO(123L);

        assertThat(result.getStaffId()).isEqualTo(123L);
        assertThat(result.getShopCode()).isEqualTo("SHOP001");
        assertThat(result.getShopProvince()).isEqualTo("HN");
        assertThat(result.getShopDistrict()).isEqualTo("BD");
        assertThat(result.getShopPrecinct()).isEqualTo("P1");
        assertThat(result.getShopChanelTypeId()).isEqualTo(7L);
    }

    @Test
    void resolveStaffDTO_byStaffId_nullResponse_throwsBusinessException() {
        when(staffShopClient.getStaffShopFullInfoByStaffId(999L)).thenReturn(null);

        BusinessException ex = catchThrowableOfType(
                () -> helper.resolveStaffDTO(999L), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo("BCCS-POLICY-MAPACTIVE-0006");
    }
}
