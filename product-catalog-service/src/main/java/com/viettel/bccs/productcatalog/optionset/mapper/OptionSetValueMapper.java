package com.viettel.bccs.productcatalog.optionset.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.entity.OptionSetValueEntity;

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

    /**
     * Map 1 dong ket qua tho (Object[]) tu native query
     * {@code OptionSetValueRepositoryCustom.findByOptionSetCodes} sang {@link OptionSetValueEntity}.
     * Chuyen tu OptionSetValueService sang day - dung dung tang trach nhiem cua Mapper.
     */
    public OptionSetValueEntity buildEntityFromRow(Object[] row) {
        return OptionSetValueEntity.builder()
                .optionSetValueId(row[0] != null ? ((Number) row[0]).longValue() : null)
                .optionSetId(row[1] != null ? ((Number) row[1]).longValue() : null)
                .name(row[2] != null ? row[2].toString() : null)
                .value(row[3] != null ? row[3].toString() : null)
                .status(row[4] != null ? row[4].toString() : null)
                .description(row[5] != null ? row[5].toString() : null)
                .createUser(row[6] != null ? row[6].toString() : null)
                .createDatetime(toDate(row[7]))
                .updateUser(row[8] != null ? row[8].toString() : null)
                .updateDatetime(toDate(row[9]))
                .parentId(row[10] != null ? ((Number) row[10]).longValue() : null)
                .build();
    }

    /**
     * Hibernate 7 tra ve cot Oracle DATE tu native query duoi dang {@link java.time.LocalDateTime},
     * khong phai {@link java.util.Date}/{@link java.sql.Timestamp} nhu ban chat driver cu - phai
     * convert thu cong ve {@link java.util.Date} de khop kieu field cua {@link OptionSetValueEntity}.
     */
    private static java.util.Date toDate(Object value) {
        if (value instanceof java.time.LocalDateTime ldt) {
            return java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        return value instanceof java.util.Date d ? d : null;
    }
}