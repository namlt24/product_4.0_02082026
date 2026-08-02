package com.viettel.bccs.productcatalog.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.common.dto.GetTextFromBundleHelper;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class BaseMessage implements Serializable {

    private String errorCode;

    /**
     * @deprecated Không dùng nữa. Thông báo từ service trả về dùng keyMsg và paramMsg.
     */
    @Deprecated
    private transient String description;

    private boolean success;
    private String keyMsg;
    private String[] paramsMsg;
    private Long returnDevice;
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