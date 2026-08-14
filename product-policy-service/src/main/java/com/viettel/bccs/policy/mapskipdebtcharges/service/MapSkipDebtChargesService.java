package com.viettel.bccs.policy.mapskipdebtcharges.service;

import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTOFull;
import com.viettel.bccs.policy.mapskipdebtcharges.mapper.MapSkipDebtChargesMapper;
import com.viettel.bccs.policy.mapskipdebtcharges.repository.MapSkipDebtChargesRepository;
import com.viettel.bccs.policy.utils.DataUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapSkipDebtChargesService {

    private final MapSkipDebtChargesRepository repository;
    private final MapSkipDebtChargesMapper mapper;

    /**
     * Với mỗi phần tử đầu vào (dùng làm mẫu tìm kiếm - example), tìm các bản ghi MAP_SKIP_DEBT_CHARGES
     * khớp và gom vào 1 {@link MapSkipDebtChargesDTOFull}, key = vị trí (index) của mẫu trong danh
     * sách đầu vào để caller đối chiếu ngược lại phần tử nào sinh ra kết quả nào.
     */
    public List<MapSkipDebtChargesDTOFull> getFullInfo(@Valid List<@Valid MapSkipDebtChargesDTO> mapSkipDebtChargesDTOs) throws Exception {
        List<MapSkipDebtChargesDTOFull> result = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(mapSkipDebtChargesDTOs)) {
            return result;
        }
        for (int i = 0; i < mapSkipDebtChargesDTOs.size(); i++) {
            List<MapSkipDebtChargesDTO> findList = findByExample(mapSkipDebtChargesDTOs.get(i));
            result.add(MapSkipDebtChargesDTOFull.builder()
                    .key(String.valueOf(i))
                    .mapSkipDebtChargesDTOList(findList)
                    .build());
        }
        return result;
    }

    private List<MapSkipDebtChargesDTO> findByExample(MapSkipDebtChargesDTO example) throws Exception {
        try {
            List<MapSkipDebtChargesDTO> found = mapper.toDTO(repository.findByExample(example));
            return found == null ? new ArrayList<>() : found;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
