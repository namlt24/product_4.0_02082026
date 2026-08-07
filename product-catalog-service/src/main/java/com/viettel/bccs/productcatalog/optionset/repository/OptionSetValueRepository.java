package com.viettel.bccs.productcatalog.optionset.repository;

import com.viettel.bccs.productcatalog.optionset.entity.OptionSetValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionSetValueRepository extends JpaRepository<OptionSetValueEntity, Long> {

    List<OptionSetValueEntity> findByOptionSetIdAndStatus(Long optionSetId, String status);

    List<OptionSetValueEntity> findByOptionSetId(Long optionSetId);

    @Query("SELECT osv FROM OptionSetValueEntity osv JOIN OptionSetEntity os ON osv.optionSetId = os.optionSetId WHERE os.code = :code AND os.status = '1'")
    List<OptionSetValueEntity> findByOptionSetCode(@Param("code") String code);

    @Query(value = "SELECT osv.*, os.code AS option_set_code " +
            "FROM option_set_value osv " +
            "JOIN option_set os ON osv.option_set_id = os.option_set_id " +
            "WHERE os.code IN :codes AND os.status = '1' " +
            "ORDER BY os.code, osv.option_set_value_id",
            nativeQuery = true)
    List<Object[]> findByOptionSetCodes(@Param("codes") List<String> codes);

    @Query(value = "SELECT osv.value FROM option_set_value osv " +
            "JOIN option_set os ON osv.option_set_id = os.option_set_id " +
            "WHERE os.code = :optSetCode AND os.status = '1' AND osv.status = '1' AND osv.name = :code",
            nativeQuery = true)
    String findValueByTwoCodeOption(@Param("optSetCode") String optSetCode, @Param("code") String name);

    @Query(value = "SELECT osv.* FROM option_set_value osv " +
            "JOIN option_set os ON osv.option_set_id = os.option_set_id " +
            "WHERE os.code = :code AND os.status = '1' AND osv.value = :value AND osv.status = '1'",
            nativeQuery = true)
    Optional<OptionSetValueEntity> findOneByCodeAndValue(@Param("code") String code, @Param("value") String value);
}