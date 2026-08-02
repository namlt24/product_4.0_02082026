package com.viettel.bccs.productcatalog.optionset.mapper;

import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetResponse;
import com.viettel.bccs.productcatalog.optionset.entity.OptionSetEntity;
import org.springframework.stereotype.Component;

@Component
public class OptionSetMapper {

    public OptionSetResponse toResponse(OptionSetEntity entity) {
        return new OptionSetResponse(
                entity.getOptionSetId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getDescription());
    }
}