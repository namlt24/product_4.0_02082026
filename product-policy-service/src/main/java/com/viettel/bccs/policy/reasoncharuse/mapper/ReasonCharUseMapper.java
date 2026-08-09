package com.viettel.bccs.policy.reasoncharuse.mapper;

import com.viettel.bccs.policy.reasoncharuse.dto.response.ReasonCharUseDTO;
import com.viettel.bccs.policy.reasoncharuse.entity.ReasonCharUseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReasonCharUseMapper {

    public ReasonCharUseDTO toDTO(ReasonCharUseEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReasonCharUseDTO.builder()
                .reasonCharUseId(entity.getReasonCharUseId())
                .reasonId(entity.getReasonId())
                .productSpecCharValueId(entity.getProductSpecCharValueId())
                .productSpecCharId(entity.getProductSpecCharId())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .status(entity.getStatus())
                .specificValue(entity.getSpecificValue())
                .limited(entity.getLimited())
                .limited2(entity.getLimited2())
                .min(entity.getMin())
                .max(entity.getMax())
                .build();
    }

    public List<ReasonCharUseDTO> toDTO(List<ReasonCharUseEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<ReasonCharUseDTO> result = new ArrayList<>(entities.size());
        for (ReasonCharUseEntity entity : entities) {
            result.add(toDTO(entity));
        }
        return result;
    }
}
