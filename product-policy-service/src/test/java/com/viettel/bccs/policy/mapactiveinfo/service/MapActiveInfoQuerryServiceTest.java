package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.StaffExtClient;
import com.viettel.bccs.policy.client.TelecomServiceClient;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.common.helper.StaffResolveHelper;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.elasticsearch.MapActiveInfoElasticsearchService;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho {@link MapActiveInfoQuerryService#getChanelTypeIdMapActiveInfo(ChanelTypeIdRequest)}
 * — overload MỚI (resolve StaffDTO qua staffId thay vì nhận raw field từ client). Không chạm tới
 * overload {@link MapActiveInfoQuerryService#getChanelTypeIdMapActiveInfo(StaffDTO)}, chỉ verify
 * nó vẫn được gọi đúng với StaffDTO đã resolve (business logic decode channelType giữ nguyên,
 * test qua đây gián tiếp để đảm bảo không regression).
 */
@ExtendWith(MockitoExtension.class)
class MapActiveInfoQuerryServiceTest {

    @Mock
    private MapActiveInfoRepository repository;
    @Mock
    private MapActiveInfoMapper mapper;
    @Mock
    private OptionSetClient optionSetClient;
    @Mock
    private TelecomServiceClient telecomServiceClient;
    @Mock
    private MapActiveInfoValidateService mapActiveInfoValidateService;
    @Mock
    private StaffExtClient staffExtClient;
    @Mock
    private ReasonService reasonService;
    @Mock
    private StaffResolveHelper staffResolveHelper;
    @Mock
    private ObjectProvider<MapActiveInfoElasticsearchService> mapActiveInfoElasticsearchServiceProvider;

    private MapActiveInfoQuerryService service;

    @BeforeEach
    void setUp() {
        service = new MapActiveInfoQuerryService(repository, mapper, optionSetClient, telecomServiceClient,
                mapActiveInfoValidateService, staffExtClient, reasonService, staffResolveHelper,
                mapActiveInfoElasticsearchServiceProvider);
    }

    private static StaffDTO staffDTO(Long shopChanelTypeId, Long channelTypeId, String pointOfSale) {
        StaffDTO dto = new StaffDTO();
        dto.setShopChanelTypeId(shopChanelTypeId);
        dto.setChannelTypeId(channelTypeId);
        dto.setPointOfSale(pointOfSale);
        return dto;
    }

    @Test
    void getChanelTypeIdMapActiveInfo_extractsStaffIdFromRequest_andResolvesViaHelper() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder()
                .staffId(999L)
                .shopId(1L)
                .shopChanelTypeId(2L)
                .build();
        when(staffResolveHelper.resolveStaffDTO(999L))
                .thenReturn(staffDTO(5L, Const.CHANNEL_TYPE.CHANNEL_TYPE_NV, null));

        service.getChanelTypeIdMapActiveInfo(request);

        verify(staffResolveHelper).resolveStaffDTO(eq(999L));
    }

    @Test
    void getChanelTypeIdMapActiveInfo_channelTypeNv_returnsShopChanelTypeId() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder().staffId(1L).build();
        when(staffResolveHelper.resolveStaffDTO(1L))
                .thenReturn(staffDTO(80L, Const.CHANNEL_TYPE.CHANNEL_TYPE_NV, null));

        Long result = service.getChanelTypeIdMapActiveInfo(request);

        assertThat(result).isEqualTo(80L);
    }

    @Test
    void getChanelTypeIdMapActiveInfo_channelTypeNvdb_pointOfSaleDb_returnsDecodedDbChannelType() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder().staffId(2L).build();
        when(staffResolveHelper.resolveStaffDTO(2L))
                .thenReturn(staffDTO(80L, Const.CHANNEL_TYPE.CHANNEL_TYPE_NVDB, Const.CHANNEL_TYPE.POINT_OF_SALE_DB));

        Long result = service.getChanelTypeIdMapActiveInfo(request);

        assertThat(result).isEqualTo(Const.CHANNEL_TYPE.CHANNEL_TYPE_DECODE_DB_POINT_OF_SALE);
    }

    @Test
    void getChanelTypeIdMapActiveInfo_channelTypeNvdb_pointOfSaleNvdb_returnsDecodedNvdbChannelType() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder().staffId(3L).build();
        when(staffResolveHelper.resolveStaffDTO(3L))
                .thenReturn(staffDTO(80L, Const.CHANNEL_TYPE.CHANNEL_TYPE_NVDB, Const.CHANNEL_TYPE.POINT_OF_SALE_NVDB));

        Long result = service.getChanelTypeIdMapActiveInfo(request);

        assertThat(result).isEqualTo(Const.CHANNEL_TYPE.CHANNEL_TYPE_DECODE_NVDB_POINT_OF_SALE);
    }

    @Test
    void getChanelTypeIdMapActiveInfo_otherChannelType_returnsChannelTypeIdItself() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder().staffId(4L).build();
        when(staffResolveHelper.resolveStaffDTO(4L))
                .thenReturn(staffDTO(80L, 999L, null));

        Long result = service.getChanelTypeIdMapActiveInfo(request);

        assertThat(result).isEqualTo(999L);
    }

    @Test
    void getChanelTypeIdMapActiveInfo_shopChanelTypeIdNull_returnsNull() {
        ChanelTypeIdRequest request = ChanelTypeIdRequest.builder().staffId(5L).build();
        when(staffResolveHelper.resolveStaffDTO(5L))
                .thenReturn(staffDTO(null, Const.CHANNEL_TYPE.CHANNEL_TYPE_NV, null));

        Long result = service.getChanelTypeIdMapActiveInfo(request);

        assertThat(result).isNull();
    }
}
