package com.viettel.bccs.productcatalog.productspecchar.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_SPEC_CHAR (xem
 * ProductSpecCharEntity) — field không nullable ở DB vẫn cho phép null ở response record
 * (record component không có @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record ProductSpecCharResponse(

        @Schema(description = "ID thuộc tính sản phẩm")
        @Min(value = 1, message = "productSpecCharId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
        Long productSpecCharId,

        @Schema(description = "Tên thuộc tính sản phẩm")
        @Size(max = 500, message = "name tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Mô tả")
        @Size(max = 512, message = "description tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
        String description,

        @Schema(description = "Loại giá trị")
        @Size(max = 2, message = "valueType tối đa 2 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2}$", message = "valueType không được chứa ký tự điều khiển")
        String valueType,

        @Schema(description = "Loại thuộc tính", example = "2")
        @Size(max = 2, message = "charType tối đa 2 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2}$", message = "charType không được chứa ký tự điều khiển")
        String charType,

        @Schema(description = "Số lượng giá trị tối thiểu")
        @Min(value = 0, message = "minCardinality phải >= 0")
        @Max(value = 9999999999L, message = "minCardinality vượt quá độ dài cột (precision 10)")
        Long minCardinality,

        @Schema(description = "Số lượng giá trị tối đa")
        @Min(value = 0, message = "maxCardinality phải >= 0")
        @Max(value = 9999999999L, message = "maxCardinality vượt quá độ dài cột (precision 10)")
        Long maxCardinality,

        @Schema(description = "Trạng thái", example = "1")
        @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
        String status,

        @Schema(description = "Người tạo", example = "system")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Thời điểm tạo")
        Date createDatetime,

        @Schema(description = "Người cập nhật", example = "system")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Thời điểm cập nhật")
        Date updateDatetime,

        @Schema(description = "Mã thuộc tính sản phẩm")
        @Size(max = 200, message = "code tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
        String code,

        @Schema(description = "ID loại thuộc tính sản phẩm")
        @Size(max = 100, message = "productSpecCharTypeId tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$",
                message = "productSpecCharTypeId không được chứa ký tự điều khiển")
        String productSpecCharTypeId,

        @Schema(description = "Kiểu tập giá trị")
        @Min(value = 0, message = "valueSetType phải >= 0")
        @Max(value = 9, message = "valueSetType vượt quá độ dài cột (precision 1)")
        Long valueSetType,

        @Schema(description = "Class xử lý response")
        @Size(max = 50, message = "responseClass tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "responseClass không được chứa ký tự điều khiển")
        String responseClass,

        @Schema(description = "Câu lệnh SQL truy vấn")
        @Size(max = 500, message = "sqlQuery tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "sqlQuery không được chứa ký tự điều khiển")
        String sqlQuery,

        @Schema(description = "Đối tượng hiển thị")
        @Size(max = 50, message = "displayObject tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "displayObject không được chứa ký tự điều khiển")
        String displayObject,

        @Schema(description = "Đối tượng giá trị")
        @Size(max = 50, message = "valueObject tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "valueObject không được chứa ký tự điều khiển")
        String valueObject,

        @Schema(description = "Câu truy vấn Solr")
        @Size(max = 500, message = "solrQuery tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "solrQuery không được chứa ký tự điều khiển")
        String solrQuery,

        @Schema(description = "Solr core")
        @Size(max = 50, message = "solrCore tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "solrCore không được chứa ký tự điều khiển")
        String solrCore,

        @Schema(description = "Solr schema")
        @Size(max = 50, message = "solrSchema tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "solrSchema không được chứa ký tự điều khiển")
        String solrSchema,

        @Schema(description = "Kiểu dữ liệu")
        @Size(max = 40, message = "dataType tối đa 40 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,40}$", message = "dataType không được chứa ký tự điều khiển")
        String dataType,

        @Schema(description = "WSDL web service")
        @Size(max = 4000, message = "wsWsdl tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "wsWsdl không được chứa ký tự điều khiển")
        String wsWsdl,

        @Schema(description = "Mẫu request")
        @Size(max = 10000, message = "templateRequest tối đa 10000 ký tự")
        String templateRequest,

        @Schema(description = "Pattern kiểm tra giá trị")
        @Size(max = 1000, message = "validatePattern tối đa 1000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "validatePattern không được chứa ký tự điều khiển")
        String validatePattern,

        @Schema(description = "Dữ liệu mở rộng")
        @Size(max = 1000, message = "extData tối đa 1000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "extData không được chứa ký tự điều khiển")
        String extData,

        @Schema(description = "Ghi chú")
        @Size(max = 256, message = "note tối đa 256 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,256}$", message = "note không được chứa ký tự điều khiển")
        String note
) {
}
