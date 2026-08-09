package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecCharDTO {

    @Schema(description = "ID đặc tính sản phẩm", example = "12345")
    private Long productSpecCharId;

    @Schema(description = "Tên đặc tính", example = "Tên đặc tính sản phẩm")
    private String name;

    @Schema(description = "Mô tả", example = "Mô tả đặc tính")
    private String description;

    @Schema(description = "Loại giá trị", example = "TEXT")
    private String valueType;

    @Schema(description = "Loại đặc tính", example = "INPUT")
    private String charType;

    @Schema(description = "Số lượng tối thiểu", example = "1")
    private Long minCardinality;

    @Schema(description = "Số lượng tối đa", example = "10")
    private Long maxCardinality;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Mã đặc tính", example = "CHAR_CODE")
    private String code;

    @Schema(description = "ID loại đặc tính sản phẩm", example = "100")
    private String productSpecCharTypeId;

    @Schema(description = "Loại tập giá trị", example = "1")
    private Long valueSetType;

    @Schema(description = "Lớp response", example = "ResponseClass")
    private String responseClass;

    @Schema(description = "Query SQL", example = "SELECT * FROM ...")
    private String sqlQuery;

    @Schema(description = "Đối tượng hiển thị", example = "displayObject")
    private String displayObject;

    @Schema(description = "Đối tượng giá trị", example = "valueObject")
    private String valueObject;

    @Schema(description = "Query Solr", example = "*:*")
    private String solrQuery;

    @Schema(description = "Core Solr", example = "solr_core")
    private String solrCore;

    @Schema(description = "Schema Solr", example = "solr_schema")
    private String solrSchema;

    @Schema(description = "Loại dữ liệu", example = "STRING")
    private String dataType;

    @Schema(description = "WSDL WebService", example = "http://wsdl.url")
    private String wsWsdl;

    @Schema(description = "Template request", example = "<soap:Envelope>...")
    private String templateRequest;

    @Schema(description = "Pattern validation", example = "^[A-Z0-9]+$")
    private String validatePattern;

    @Schema(description = "Dữ liệu mở rộng", example = "{}")
    private String extData;

    @Schema(description = "Ghi chú", example = "Ghi chú")
    private String note;

    @Schema(description = "ID sản phẩm", example = "111")
    private Long productOfferingId;

    @Schema(description = "ID sử dụng đặc tính sản phẩm", example = "222")
    private Long offerCharUseId;

    @Schema(description = "Loại sử dụng đặc tính", example = "MAIN")
    private String offerCharUseType;

    @Schema(description = "Tên giá trị", example = "Value Name")
    private String valueName;

    @Schema(description = "Giá trị đặc tính sản phẩm")
    private ProductSpecCharValueDTO productSpecCharValueDTO;
}