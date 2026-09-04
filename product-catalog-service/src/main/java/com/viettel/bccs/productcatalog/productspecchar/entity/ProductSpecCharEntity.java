package com.viettel.bccs.productcatalog.productspecchar.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_SPEC_CHAR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecCharEntity {

    @Id
    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
    private Long productSpecCharId;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "VALUE_TYPE", length = 2)
    private String valueType;

    @Column(name = "CHAR_TYPE", length = 2)
    private String charType;

    @Column(name = "MIN_CARDINALITY", precision = 10)
    private Long minCardinality;

    @Column(name = "MAX_CARDINALITY", precision = 10)
    private Long maxCardinality;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "CODE", length = 200)
    private String code;

    @Column(name = "PRODUCT_SPEC_CHAR_TYPE_ID", length = 100)
    private String productSpecCharTypeId;

    @Column(name = "VALUE_SET_TYPE", precision = 1)
    private Long valueSetType;

    @Column(name = "RESPONSE_CLASS", length = 50)
    private String responseClass;

    @Column(name = "SQL_QUERY", length = 500)
    private String sqlQuery;

    @Column(name = "DISPLAY_OBJECT", length = 50)
    private String displayObject;

    @Column(name = "VALUE_OBJECT", length = 50)
    private String valueObject;

    @Column(name = "SOLR_QUERY", length = 500)
    private String solrQuery;

    @Column(name = "SOLR_CORE", length = 50)
    private String solrCore;

    @Column(name = "SOLR_SCHEMA", length = 50)
    private String solrSchema;

    @Column(name = "DATA_TYPE", length = 40)
    private String dataType;

    @Column(name = "WS_WSDL", length = 4000)
    private String wsWsdl;

    @Column(name = "TEMPLATE_REQUEST", columnDefinition = "CLOB")
    private String templateRequest;

    @Column(name = "VALIDATE_PATTERN", length = 1000)
    private String validatePattern;

    @Column(name = "EXT_DATA", length = 1000)
    private String extData;

    @Column(name = "NOTE", length = 256)
    private String note;
}