package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Min(value = 1, message = "productSpecCharId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharId;

    @Schema(description = "Tên đặc tính", example = "Tên đặc tính sản phẩm")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mô tả", example = "Mô tả đặc tính")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Loại giá trị", example = "TEXT")
    @Size(max = 2, message = "valueType tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "valueType chỉ gồm chữ và số")
    private String valueType;

    @Schema(description = "Loại đặc tính", example = "INPUT")
    @Size(max = 2, message = "charType tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "charType chỉ gồm chữ và số")
    private String charType;

    @Schema(description = "Số lượng tối thiểu", example = "1")
    @Min(value = 0, message = "minCardinality phải >= 0")
    @Max(value = 9999999999L, message = "minCardinality vượt quá độ dài cột (precision 10)")
    private Long minCardinality;

    @Schema(description = "Số lượng tối đa", example = "10")
    @Min(value = 0, message = "maxCardinality phải >= 0")
    @Max(value = 9999999999L, message = "maxCardinality vượt quá độ dài cột (precision 10)")
    private Long maxCardinality;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Mã đặc tính", example = "CHAR_CODE")
    @Size(max = 200, message = "code tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "ID loại đặc tính sản phẩm", example = "100")
    @Size(max = 100, message = "productSpecCharTypeId tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "productSpecCharTypeId chỉ gồm chữ, số, '_' hoặc '-'")
    private String productSpecCharTypeId;

    @Schema(description = "Loại tập giá trị", example = "1")
    @Min(value = 0, message = "valueSetType phải >= 0")
    @Max(value = 9, message = "valueSetType vượt quá độ dài cột (precision 1)")
    private Long valueSetType;

    @Schema(description = "Lớp response", example = "ResponseClass")
    @Size(max = 50, message = "responseClass tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "responseClass không được chứa ký tự điều khiển")
    private String responseClass;

    @Schema(description = "Query SQL", example = "SELECT * FROM ...")
    @Size(max = 500, message = "sqlQuery tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "sqlQuery không được chứa ký tự điều khiển")
    private String sqlQuery;

    @Schema(description = "Đối tượng hiển thị", example = "displayObject")
    @Size(max = 50, message = "displayObject tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "displayObject không được chứa ký tự điều khiển")
    private String displayObject;

    @Schema(description = "Đối tượng giá trị", example = "valueObject")
    @Size(max = 50, message = "valueObject tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "valueObject không được chứa ký tự điều khiển")
    private String valueObject;

    @Schema(description = "Query Solr", example = "*:*")
    @Size(max = 500, message = "solrQuery tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "solrQuery không được chứa ký tự điều khiển")
    private String solrQuery;

    @Schema(description = "Core Solr", example = "solr_core")
    @Size(max = 50, message = "solrCore tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "solrCore không được chứa ký tự điều khiển")
    private String solrCore;

    @Schema(description = "Schema Solr", example = "solr_schema")
    @Size(max = 50, message = "solrSchema tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "solrSchema không được chứa ký tự điều khiển")
    private String solrSchema;

    @Schema(description = "Loại dữ liệu", example = "STRING")
    @Size(max = 40, message = "dataType tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "dataType chỉ gồm chữ, số, '_' hoặc '-'")
    private String dataType;

    @Schema(description = "WSDL WebService", example = "http://wsdl.url")
    @Size(max = 4000, message = "wsWsdl tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "wsWsdl không được chứa ký tự điều khiển")
    private String wsWsdl;

    @Schema(description = "Template request", example = "<soap:Envelope>...")
    @Size(max = 100000, message = "templateRequest tối đa 100000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100000}$", message = "templateRequest không được chứa ký tự điều khiển")
    private String templateRequest;

    @Schema(description = "Pattern validation", example = "^[A-Z0-9]+$")
    @Size(max = 1000, message = "validatePattern tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "validatePattern không được chứa ký tự điều khiển")
    private String validatePattern;

    @Schema(description = "Dữ liệu mở rộng", example = "{}")
    @Size(max = 1000, message = "extData tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "extData không được chứa ký tự điều khiển")
    private String extData;

    @Schema(description = "Ghi chú", example = "Ghi chú")
    @Size(max = 256, message = "note tối đa 256 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,256}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Schema(description = "ID sản phẩm", example = "111")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "ID sử dụng đặc tính sản phẩm", example = "222")
    @Min(value = 1, message = "offerCharUseId phải >= 1")
    @Max(value = 9999999999L, message = "offerCharUseId vượt quá độ dài cột (precision 10)")
    private Long offerCharUseId;

    @Schema(description = "Loại sử dụng đặc tính", example = "MAIN")
    @Size(max = 1, message = "offerCharUseType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "offerCharUseType chỉ gồm chữ hoặc số")
    private String offerCharUseType;

    @Schema(description = "Tên giá trị", example = "Value Name")
    @Size(max = 500, message = "valueName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "valueName không được chứa ký tự điều khiển")
    private String valueName;

    @Schema(description = "Giá trị đặc tính sản phẩm")
    private ProductSpecCharValueDTO productSpecCharValueDTO;
}
