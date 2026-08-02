package com.viettel.bccs.productcatalog.optionset.mapper;

import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.entity.OptionSetValueEntity;
import org.springframework.stereotype.Component;

@Component
public class OptionSetValueMapper {

    public OptionSetValueResponse toResponse(OptionSetValueEntity entity) {
        return toResponse(entity, null);
    }

    public OptionSetValueResponse toResponse(OptionSetValueEntity entity, String optionSetCode) {
        return new OptionSetValueResponse(
                entity.getOptionSetValueId(),
                entity.getOptionSetId(),
                optionSetCode,
                entity.getName(),
                entity.getValue(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getParentId());
    }
}