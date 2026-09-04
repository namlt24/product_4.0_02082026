package com.viettel.bccs.productcatalog.common.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.common.dto.GetTextFromBundleHelper;
import com.viettel.bccs.productcatalog.utils.DataUtil;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class BaseMessage implements Serializable {

    @Schema(description = "Mã lỗi", example = "BCCS-CATALOG-OPTION-0001")
    @Size(max = 100, message = "errorCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "errorCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String errorCode;

    /**
     * @deprecated Không dùng nữa. Thông báo từ service trả về dùng keyMsg và paramMsg.
     */
    @Deprecated
    @Schema(description = "Mô tả lỗi (deprecated, dùng keyMsg/paramsMsg)", example = "Option set not found")
    @Size(max = 1000, message = "description tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "description không được chứa ký tự điều khiển")
    private transient String description;

    private boolean success;

    @Schema(description = "Khoá bản tin trong resource bundle", example = "error.optionset.notfound")
    @Size(max = 200, message = "keyMsg tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_.-]{0,200}$", message = "keyMsg chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String keyMsg;

    @Size(max = 50, message = "paramsMsg tối đa 50 phần tử")
    private String[] paramsMsg;

    @Schema(description = "Id thiết bị trả về", example = "1")
    @Min(value = 0, message = "returnDevice phải >= 0")
    @Max(value = 9999999999L, message = "returnDevice vượt quá độ dài cột (precision 10)")
    private Long returnDevice;

    @Schema(description = "Danh sách sản phẩm liên quan")
    @Size(max = 1000, message = "lstProduct tối đa 1000 phần tử")
    private List<ProductOfferOutputDTO> lstProduct;

    public BaseMessage(boolean success) {
        this.success = success;
    }

    public BaseMessage(String errorCode, boolean success) {
        this.errorCode = errorCode;
        this.success = success;
    }

    public BaseMessage(boolean success, String errorCode, String keyMsg, String... paramsMsg) {
        this.errorCode = errorCode;
        this.success = success;
        setKeyMsg(keyMsg, paramsMsg);
    }

    public BaseMessage(String errorCode, boolean success, String description) {
        this.errorCode = errorCode;
        this.description = description;
        this.success = success;
    }

    public BaseMessage(BaseMessage msg) {
        this.errorCode = msg.errorCode;
        this.description = msg.description;
        this.success = msg.success;
        this.keyMsg = msg.keyMsg;
        this.paramsMsg = msg.paramsMsg;
    }

    public void setKeyMsg(String keyMsg) {
        if (!DataUtil.isNullOrEmpty(keyMsg)) {
            this.description = GetTextFromBundleHelper.getText(keyMsg);
        }
        this.keyMsg = keyMsg;
    }

    public void setKeyMsg(String keyMsg, String... params) {
        if (!DataUtil.isNullOrEmpty(keyMsg)) {
            if (DataUtil.isNullOrEmpty(params)) {
                this.description = GetTextFromBundleHelper.getText(keyMsg);
            } else {
                this.description = GetTextFromBundleHelper.getTextParam(keyMsg, params);
            }
        }
        this.paramsMsg = params;
        this.keyMsg = keyMsg;
    }

    public void setErrorCodeAddLine(String errorCode) {
        this.errorCode = ((this.errorCode != null) ? this.errorCode + "; " : "") + errorCode;
    }

    public String getDescription() {
        if (DataUtil.isNullOrEmpty(description)) {
            if (DataUtil.isNullOrEmpty(paramsMsg)) {
                return GetTextFromBundleHelper.getText(keyMsg);
            } else {
                return GetTextFromBundleHelper.getTextParam(keyMsg, paramsMsg);
            }
        }
        return description;
    }

    @JsonIgnore
    public BaseMessage getBaseMsg() {
        return new BaseMessage(this);
    }
}