package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.error.BccsClient4xxException;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustTypeClientImpl implements CustTypeClient {

    private final OrganizationResourceFeignClient organizationResourceFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CustTypeDTO> findActiveByCustType(String custType, String status) {
        try {
            var response = organizationResourceFeignClient.findActiveByCustType(custType).getBody();
            if (response != null && response.getData() != null) {
                CustTypeDTO dto = objectMapper.convertValue(response.getData(), CustTypeDTO.class);
                return Optional.of(dto);
            }
            return Optional.empty();
        } catch (BccsClient4xxException e) {
            // Bat ky loi 4xx nao tu findActiveByCustType (404 "khong tim thay" TU BusinessException
            // BCCS-ORGANIZATION-CUSTTYPE-0001, hoac 400 validation neu custType khong hop le - VD
            // qua do dai @Size(max=6) cua param) deu co cung y nghia voi phia goi: custType nay
            // khong ung voi du lieu hop le nao -> tra "khong co du lieu", khong phai loi he thong.
            // Endpoint nay khong co ly do 4xx nao khac ngoai 2 truong hop tren. Giu nguyen
            // IntegrationException cho loi 5xx/mat ket noi that (nhanh catch RuntimeException ben duoi).
            log.info("custType không hợp lệ hoặc không tồn tại (HTTP {}): custType={}, errorCode={}",
                    e.getStatusCode(), custType, e.getErrorCode());
            return Optional.empty();
        } catch (RuntimeException e) {
            log.error("Error calling findActiveByCustType for custType={}, status={}", custType, status, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service findActiveByCustType for custType=" + custType, e);
        }
    }
}