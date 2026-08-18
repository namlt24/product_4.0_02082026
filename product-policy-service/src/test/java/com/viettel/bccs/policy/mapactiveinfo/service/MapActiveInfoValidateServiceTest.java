package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.ProductOfferCharUseClient;
import com.viettel.bccs.policy.client.ProductOfferingClient;
import com.viettel.bccs.policy.client.StaffExtClient;
import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.StaffShopClient;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.mapping.service.MappingService;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.MessageUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cho phần ORCHESTRATION song song mới của {@link MapActiveInfoValidateService#validateMapActiveInfo}
 * (nhánh {@code offerIds}, xem kế hoạch/plan liên quan tới việc parallelize vòng lặp offerIds để giảm
 * độ trễ khi deploy trên k8s). {@link MapActiveInfoValidateService#resolveOneOffer} được stub qua spy
 * (business logic bên trong hàm đó KHÔNG đổi - extract nguyên vẹn từ code tuần tự cũ, không cần test
 * lại ở đây) - test này chỉ verify: (1) thứ tự kết quả/lỗi theo đúng vị trí offerId trong list đầu
 * vào, không theo thứ tự hoàn thành song song, (2) mỗi offerId task được bọc transaction riêng, (3)
 * timeout từng task không làm treo cả request.
 */
@ExtendWith(MockitoExtension.class)
class MapActiveInfoValidateServiceTest {

    @Mock
    private MapActiveInfoRepository repository;
    @Mock
    private MapActiveInfoMapper mapper;
    @Mock
    private OptionSetClient optionSetClient;
    @Mock
    private StaffShopClient staffShopClient;
    @Mock
    private StaffExtClient staffExtClient;
    @Mock
    private ProductOfferingClient productOfferingClient;
    @Mock
    private ProductOfferCharUseClient productOfferCharUseClient;
    @Mock
    private ReasonService reasonService;
    @Mock
    private DiscountPromotionService discountPromotionService;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private MessageUtil messageUtil;
    @Mock
    private MapActiveInfoQuerryService mapActiveInfoQuerryService;
    @Mock
    private MappingService mappingService;

    private ExecutorService asyncExecutor;
    private MapActiveInfoValidateService spyService;

    @BeforeEach
    void setUp() {
        asyncExecutor = Executors.newFixedThreadPool(4);

        MapActiveInfoValidateService realService = new MapActiveInfoValidateService(repository, mapper,
                optionSetClient, staffShopClient, staffExtClient, productOfferingClient, productOfferCharUseClient,
                reasonService, discountPromotionService, transactionTemplate, asyncExecutor, messageUtil,
                mapActiveInfoQuerryService, mappingService);
        ReflectionTestUtils.setField(realService, "taskTimeoutMs", 2000L);
        ReflectionTestUtils.setField(realService, "totalTimeoutMs", 4000L);

        spyService = spy(realService);

        // transactionTemplate.execute(...) chỉ chạy thẳng callback (không mở transaction thật) -
        // đủ để verify nó ĐƯỢC GỌI, không cần hành vi transaction thật trong unit test.
        // lenient(): case offerIds rỗng không đi vào nhánh dùng các mock này - không phải lỗi test.
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        lenient().when(mapActiveInfoQuerryService.checkMapActiveInfo(any(), any())).thenReturn(true);
        lenient().when(productOfferingClient.findByIds(any())).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        asyncExecutor.shutdownNow();
    }

    private List<MapActiveInfoDTO> invoke(List<Long> offerIds) {
        return spyService.validateMapActiveInfo(null, "00", offerIds, "-1", 1L, null, 1L, null,
                false, "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "1", "-1", 1, "PRODUCT_CODE", null);
    }

    private static MapActiveInfoDTO dto(Long offerId) {
        MapActiveInfoDTO d = new MapActiveInfoDTO();
        d.setOfferId(offerId);
        return d;
    }

    @Test
    void validateMapActiveInfo_multipleOfferIds_allSucceed_returnsInOriginalOrder() throws Exception {
        Long offerId1 = 101L;
        Long offerId2 = 102L;
        Long offerId3 = 103L;

        // offerId3 (đứng CUỐI list) chạy CHẬM hơn offerId1/offerId2 -> nếu code merge theo thứ tự
        // hoàn thành thay vì thứ tự offerIds đầu vào thì test này sẽ fail.
        doReturn(dto(offerId1)).when(spyService).resolveOneOffer(any(), any(), eq(offerId1), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        doReturn(dto(offerId2)).when(spyService).resolveOneOffer(any(), any(), eq(offerId2), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        doAnswer(invocation -> {
            Thread.sleep(300);
            return dto(offerId3);
        }).when(spyService).resolveOneOffer(any(), any(), eq(offerId3), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());

        List<MapActiveInfoDTO> result = invoke(List.of(offerId1, offerId2, offerId3));

        assertThat(result).extracting(MapActiveInfoDTO::getOfferId)
                .containsExactly(offerId1, offerId2, offerId3);
    }

    @Test
    void validateMapActiveInfo_errorAtEarlierPosition_reportedEvenIfItFinishesLast() throws Exception {
        Long offerIdOk = 201L;
        Long offerIdFails = 202L; // đứng SAU offerIdOk trong list -> hành vi cũ: offerIdOk xử lý xong
                                    // trước, offerIdFails lỗi sau, exception của offerIdFails được ném.

        doAnswer(invocation -> {
            Thread.sleep(300); // offerIdOk hoàn thành SAU offerIdFails khi chạy song song
            return dto(offerIdOk);
        }).when(spyService).resolveOneOffer(any(), any(), eq(offerIdOk), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        BusinessException expectedError = new BusinessException("BCCS-POLICY-MAPACTIVE-0002", "offer khong hop le");
        doThrow(expectedError).when(spyService).resolveOneOffer(any(), any(), eq(offerIdFails), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());

        assertThatThrownBy(() -> invoke(List.of(offerIdOk, offerIdFails)))
                .isSameAs(expectedError);
    }

    @Test
    void validateMapActiveInfo_errorAtFirstPosition_matchesOldFailFastBehaviorEvenWithSecondOfferValid() throws Exception {
        Long offerIdFails = 301L;
        Long offerIdOk = 302L;

        BusinessException expectedError = new BusinessException("BCCS-POLICY-MAPACTIVE-0005");
        doThrow(expectedError).when(spyService).resolveOneOffer(any(), any(), eq(offerIdFails), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        doReturn(dto(offerIdOk)).when(spyService).resolveOneOffer(any(), any(), eq(offerIdOk), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());

        assertThatThrownBy(() -> invoke(List.of(offerIdFails, offerIdOk)))
                .isSameAs(expectedError);
    }

    @Test
    void validateMapActiveInfo_wrapsEachOfferIdInItsOwnTransaction() throws Exception {
        List<Long> offerIds = List.of(401L, 402L, 403L);
        for (Long offerId : offerIds) {
            doReturn(dto(offerId)).when(spyService).resolveOneOffer(any(), any(), eq(offerId), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        }

        invoke(offerIds);

        verify(transactionTemplate, times(offerIds.size())).execute(any());
    }

    @Test
    void validateMapActiveInfo_oneOfferIdTimesOut_doesNotHangAndSurfacesIntegrationException() throws Exception {
        Long offerIdSlow = 501L;
        Long offerIdOk = 502L;

        // taskTimeoutMs = 2000ms (set ở setUp) -> offerIdSlow ngủ lâu hơn hẳn để chắc chắn bị timeout,
        // nhưng vẫn để test tổng thể chạy nhanh (không cần chờ hết totalTimeoutMs=4000ms).
        doAnswer(invocation -> {
            Thread.sleep(6000);
            return dto(offerIdSlow);
        }).when(spyService).resolveOneOffer(any(), any(), eq(offerIdSlow), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());
        doReturn(dto(offerIdOk)).when(spyService).resolveOneOffer(any(), any(), eq(offerIdOk), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any());

        long start = System.currentTimeMillis();
        assertThatThrownBy(() -> invoke(List.of(offerIdOk, offerIdSlow)))
                .isInstanceOf(IntegrationException.class);
        long elapsedMs = System.currentTimeMillis() - start;

        // Phải kết thúc ở khoảng taskTimeoutMs/totalTimeoutMs (vài giây), KHÔNG chờ hết 6s ngủ giả lập -
        // đúng mục tiêu ban đầu: chặn latency không giới hạn.
        assertThat(elapsedMs).isLessThan(5500L);
    }

    @Test
    void validateMapActiveInfo_emptyOfferIds_throwsBusinessException0017() {
        assertThatThrownBy(() -> invoke(List.of()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("BCCS-POLICY-MAPACTIVE-0017");
    }
}
