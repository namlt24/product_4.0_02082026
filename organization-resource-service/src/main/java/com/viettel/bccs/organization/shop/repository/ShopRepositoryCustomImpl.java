package com.viettel.bccs.organization.shop.repository;

import com.viettel.bccs.organization.shop.entity.ShopEntity;
import com.viettel.bccs.organization.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implement custom repository cho Shop - xử lý batch query để tránh ORA-01795.
 */
@Repository
@RequiredArgsConstructor
public class ShopRepositoryCustomImpl implements ShopRepositoryCustom {

    private static final int BATCH_SIZE = 100;
    private final EntityManager entityManager;

    @Override
    public List<ShopEntity> findActiveByShopIds(List<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ShopEntity> results = new ArrayList<>();
        for (int i = 0; i < shopIds.size(); i += BATCH_SIZE) {
            List<Long> batch = shopIds.subList(i, Math.min(i + BATCH_SIZE, shopIds.size()));
            List<ShopEntity> batchResults = executeQuery(batch);
            results.addAll(batchResults);
        }

        return new ArrayList<>(results);
    }

    private List<ShopEntity> executeQuery(List<Long> shopIds) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < shopIds.size(); i++) {
            inClause.append(i == 0 ? ":id" + i : ", :id" + i);
        }

        String sql = """
            SELECT s.SHOP_ID, s.NAME, s.PARENT_SHOP_ID, s.ACCOUNT, s.BANK_NAME, s.ADDRESS, s.TEL, s.FAX,
                   s.SHOP_CODE, s.SHOP_TYPE, s.CONTACT_NAME, s.CONTACT_TITLE, s.TEL_NUMBER, s.EMAIL,
                   s.DESCRIPTION, s.PROVINCE, s.PAR_SHOP_CODE, s.CENTER_CODE, s.OLD_SHOP_CODE, s.COMPANY,
                   s.TIN, s.SHOP, s.PROVINCE_CODE, s.PAY_COMM, s.CREATE_DATE, s.CHANNEL_TYPE_ID,
                   s.DISCOUNT_POLICY, s.PRICE_POLICY, s.SHOP_PATH, s.DISTRICT, s.PRECINCT, s.AREA_CODE,
                   s.ID_NO, s.ID_ISSUE_PLACE, s.ID_ISSUE_DATE, s.STREET_BLOCK, s.STREET, s.HOME,
                   s.ID_TYPE, s.SHOP_PATH_NAME, s.CONTRACT_NO, s.FILE_ATTACH, s.CONTRACT_FROM_DATE,
                   s.CONTRACT_TO_DATE, s.DEPOSIT_VALUE, s.FILE_NAME, s.BUSINESS_LICENCE,
                   s.BANKPLUS_MOBILE, s.STOCK_NUM, s.STOCK_NUM_IMP, s.UPDATE_DATE_TIME, s.BANK_CODE,
                   s.SHOP_KEEPER_ID, s.SHOP_DIRECTOR_ID, s.FILE_ATTACHMENT_ID, s.GROUP_CHANNEL_TYPE_ID,
                   s.STAFF_OWNER_ID, s.TENANT_ID, s.FILE_CONTRACT_ID, s.FILE_CONTRACT_CATALOG_ID,
                   s.FILE_IDENTITY_ID, s.FILE_BUSINESS_LICENSE_ID, s.BUSINESS_PROVINCE, s.BUSINESS_DISTRICT,
                   s.BUSINESS_PRECINCT, s.BUSINESS_STREET_BLOCK, s.BUSINESS_STREET, s.BUSINESS_HOME,
                   s.BUSINESS_AREACODE, s.BUSINESS_ADDRESS, s.CREATE_USER, s.UPDATE_USER, s.CREATE_DATETIME,
                   s.STATUS, s.TURNOVER, s.BIRTHDAY
            FROM bccs_product.shop s
            WHERE s.STATUS = '1'
              AND s.SHOP_ID IN (%s)
            """.formatted(inClause);

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        query.addScalar("SHOP_ID", BigDecimal.class)
                .addScalar("NAME", String.class)
                .addScalar("PARENT_SHOP_ID", BigDecimal.class)
                .addScalar("ACCOUNT", String.class)
                .addScalar("BANK_NAME", String.class)
                .addScalar("ADDRESS", String.class)
                .addScalar("TEL", String.class)
                .addScalar("FAX", String.class)
                .addScalar("SHOP_CODE", String.class)
                .addScalar("SHOP_TYPE", String.class)
                .addScalar("CONTACT_NAME", String.class)
                .addScalar("CONTACT_TITLE", String.class)
                .addScalar("TEL_NUMBER", String.class)
                .addScalar("EMAIL", String.class)
                .addScalar("DESCRIPTION", String.class)
                .addScalar("PROVINCE", String.class)
                .addScalar("PAR_SHOP_CODE", String.class)
                .addScalar("CENTER_CODE", String.class)
                .addScalar("OLD_SHOP_CODE", String.class)
                .addScalar("COMPANY", String.class)
                .addScalar("TIN", String.class)
                .addScalar("SHOP", String.class)
                .addScalar("PROVINCE_CODE", String.class)
                .addScalar("PAY_COMM", String.class)
                .addScalar("CREATE_DATE", Date.class)
                .addScalar("CHANNEL_TYPE_ID", BigDecimal.class)
                .addScalar("DISCOUNT_POLICY", String.class)
                .addScalar("PRICE_POLICY", String.class)
                .addScalar("SHOP_PATH", String.class)
                .addScalar("DISTRICT", String.class)
                .addScalar("PRECINCT", String.class)
                .addScalar("AREA_CODE", String.class)
                .addScalar("ID_NO", String.class)
                .addScalar("ID_ISSUE_PLACE", String.class)
                .addScalar("ID_ISSUE_DATE", Date.class)
                .addScalar("STREET_BLOCK", String.class)
                .addScalar("STREET", String.class)
                .addScalar("HOME", String.class)
                .addScalar("ID_TYPE", BigDecimal.class)
                .addScalar("SHOP_PATH_NAME", String.class)
                .addScalar("CONTRACT_NO", String.class)
                .addScalar("FILE_ATTACH", byte[].class)
                .addScalar("CONTRACT_FROM_DATE", Date.class)
                .addScalar("CONTRACT_TO_DATE", Date.class)
                .addScalar("DEPOSIT_VALUE", BigDecimal.class)
                .addScalar("FILE_NAME", String.class)
                .addScalar("BUSINESS_LICENCE", String.class)
                .addScalar("BANKPLUS_MOBILE", String.class)
                .addScalar("STOCK_NUM", BigDecimal.class)
                .addScalar("STOCK_NUM_IMP", BigDecimal.class)
                .addScalar("UPDATE_DATE_TIME", Date.class)
                .addScalar("BANK_CODE", String.class)
                .addScalar("SHOP_KEEPER_ID", BigDecimal.class)
                .addScalar("SHOP_DIRECTOR_ID", BigDecimal.class)
                .addScalar("FILE_ATTACHMENT_ID", BigDecimal.class)
                .addScalar("GROUP_CHANNEL_TYPE_ID", BigDecimal.class)
                .addScalar("STAFF_OWNER_ID", BigDecimal.class)
                .addScalar("TENANT_ID", BigDecimal.class)
                .addScalar("FILE_CONTRACT_ID", BigDecimal.class)
                .addScalar("FILE_CONTRACT_CATALOG_ID", BigDecimal.class)
                .addScalar("FILE_IDENTITY_ID", BigDecimal.class)
                .addScalar("FILE_BUSINESS_LICENSE_ID", BigDecimal.class)
                .addScalar("BUSINESS_PROVINCE", String.class)
                .addScalar("BUSINESS_DISTRICT", String.class)
                .addScalar("BUSINESS_PRECINCT", String.class)
                .addScalar("BUSINESS_STREET_BLOCK", String.class)
                .addScalar("BUSINESS_STREET", String.class)
                .addScalar("BUSINESS_HOME", String.class)
                .addScalar("BUSINESS_AREACODE", String.class)
                .addScalar("BUSINESS_ADDRESS", String.class)
                .addScalar("CREATE_USER", String.class)
                .addScalar("UPDATE_USER", String.class)
                .addScalar("CREATE_DATETIME", Date.class)
                .addScalar("STATUS", String.class)
                .addScalar("TURNOVER", String.class)
                .addScalar("BIRTHDAY", Date.class);

        for (int i = 0; i < shopIds.size(); i++) {
            query.setParameter("id" + i, shopIds.get(i));
        }

        List<Tuple> rows = query.getResultList();
        List<ShopEntity> results = new ArrayList<>();
        for (Tuple row : rows) {
            results.add(mapRowToEntity(row));
        }
        return results;
    }

    private ShopEntity mapRowToEntity(Tuple row) {
        ShopEntity e = new ShopEntity();
        e.setShopId(toLong(row.get("SHOP_ID", BigDecimal.class)));
        e.setName(row.get("NAME", String.class));
        e.setParentShopId(toLong(row.get("PARENT_SHOP_ID", BigDecimal.class)));
        e.setAccount(row.get("ACCOUNT", String.class));
        e.setBankName(row.get("BANK_NAME", String.class));
        e.setAddress(row.get("ADDRESS", String.class));
        e.setTel(row.get("TEL", String.class));
        e.setFax(row.get("FAX", String.class));
        e.setShopCode(row.get("SHOP_CODE", String.class));
        e.setShopType(row.get("SHOP_TYPE", String.class));
        e.setContactName(row.get("CONTACT_NAME", String.class));
        e.setContactTitle(row.get("CONTACT_TITLE", String.class));
        e.setTelNumber(row.get("TEL_NUMBER", String.class));
        e.setEmail(row.get("EMAIL", String.class));
        e.setDescription(row.get("DESCRIPTION", String.class));
        e.setProvince(row.get("PROVINCE", String.class));
        e.setParShopCode(row.get("PAR_SHOP_CODE", String.class));
        e.setCenterCode(row.get("CENTER_CODE", String.class));
        e.setOldShopCode(row.get("OLD_SHOP_CODE", String.class));
        e.setCompany(row.get("COMPANY", String.class));
        e.setTin(row.get("TIN", String.class));
        e.setShop(row.get("SHOP", String.class));
        e.setProvinceCode(row.get("PROVINCE_CODE", String.class));
        e.setPayComm(row.get("PAY_COMM", String.class));
        e.setCreateDate(row.get("CREATE_DATE", Date.class));
        e.setChannelTypeId(toLong(row.get("CHANNEL_TYPE_ID", BigDecimal.class)));
        e.setDiscountPolicy(row.get("DISCOUNT_POLICY", String.class));
        e.setPricePolicy(row.get("PRICE_POLICY", String.class));
        e.setShopPath(row.get("SHOP_PATH", String.class));
        e.setDistrict(row.get("DISTRICT", String.class));
        e.setPrecinct(row.get("PRECINCT", String.class));
        e.setAreaCode(row.get("AREA_CODE", String.class));
        e.setIdNo(row.get("ID_NO", String.class));
        e.setIdIssuePlace(row.get("ID_ISSUE_PLACE", String.class));
        e.setIdIssueDate(row.get("ID_ISSUE_DATE", Date.class));
        e.setStreetBlock(row.get("STREET_BLOCK", String.class));
        e.setStreet(row.get("STREET", String.class));
        e.setHome(row.get("HOME", String.class));
        e.setIdType(toInt(row.get("ID_TYPE", BigDecimal.class)));
        e.setShopPathName(row.get("SHOP_PATH_NAME", String.class));
        e.setContractNo(row.get("CONTRACT_NO", String.class));
        e.setFileAttach(row.get("FILE_ATTACH", byte[].class));
        e.setContractFromDate(row.get("CONTRACT_FROM_DATE", Date.class));
        e.setContractToDate(row.get("CONTRACT_TO_DATE", Date.class));
        e.setDepositValue(toLong(row.get("DEPOSIT_VALUE", BigDecimal.class)));
        e.setFileName(row.get("FILE_NAME", String.class));
        e.setBusinessLicence(row.get("BUSINESS_LICENCE", String.class));
        e.setBankplusMobile(row.get("BANKPLUS_MOBILE", String.class));
        e.setStockNum(toInt(row.get("STOCK_NUM", BigDecimal.class)));
        e.setStockNumImp(toInt(row.get("STOCK_NUM_IMP", BigDecimal.class)));
        e.setUpdateDateTime(row.get("UPDATE_DATE_TIME", Date.class));
        e.setBankCode(row.get("BANK_CODE", String.class));
        e.setShopKeeperId(toLong(row.get("SHOP_KEEPER_ID", BigDecimal.class)));
        e.setShopDirectorId(toLong(row.get("SHOP_DIRECTOR_ID", BigDecimal.class)));
        e.setFileAttachmentId(toLong(row.get("FILE_ATTACHMENT_ID", BigDecimal.class)));
        e.setGroupChannelTypeId(toLong(row.get("GROUP_CHANNEL_TYPE_ID", BigDecimal.class)));
        e.setStaffOwnerId(toLong(row.get("STAFF_OWNER_ID", BigDecimal.class)));
        e.setTenantId(toLong(row.get("TENANT_ID", BigDecimal.class)));
        e.setFileContractId(toLong(row.get("FILE_CONTRACT_ID", BigDecimal.class)));
        e.setFileContractCatalogId(toLong(row.get("FILE_CONTRACT_CATALOG_ID", BigDecimal.class)));
        e.setFileIdentityId(toLong(row.get("FILE_IDENTITY_ID", BigDecimal.class)));
        e.setFileBusinessLicenseId(toLong(row.get("FILE_BUSINESS_LICENSE_ID", BigDecimal.class)));
        e.setBusinessProvince(row.get("BUSINESS_PROVINCE", String.class));
        e.setBusinessDistrict(row.get("BUSINESS_DISTRICT", String.class));
        e.setBusinessPrecinct(row.get("BUSINESS_PRECINCT", String.class));
        e.setBusinessStreetBlock(row.get("BUSINESS_STREET_BLOCK", String.class));
        e.setBusinessStreet(row.get("BUSINESS_STREET", String.class));
        e.setBusinessHome(row.get("BUSINESS_HOME", String.class));
        e.setBusinessAreacode(row.get("BUSINESS_AREACODE", String.class));
        e.setBusinessAddress(row.get("BUSINESS_ADDRESS", String.class));
        e.setCreateUser(row.get("CREATE_USER", String.class));
        e.setUpdateUser(row.get("UPDATE_USER", String.class));
        e.setCreateDatetime(row.get("CREATE_DATETIME", Date.class));
        e.setStatus(row.get("STATUS", String.class));
        e.setTurnover(row.get("TURNOVER", String.class));
        e.setBirthday(row.get("BIRTHDAY", Date.class));
        return e;
    }

    private Long toLong(BigDecimal value) {
        return value != null ? value.longValue() : null;
    }

    private Integer toInt(BigDecimal value) {
        return value != null ? value.intValue() : null;
    }
}