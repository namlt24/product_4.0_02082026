package com.viettel.bccs.productcatalog.utils;

public class Const {
    public static final String DEFAULT_PRODUCT_SCHEMA = "BCCS_PRODUCT.";

    public static final class RestResponseCode {
        public static final String SUCCESS = "000";
        public static final String DATA_NOT_FOUND = "1246";
        public static final String ERROR = "999";

        private RestResponseCode() {
        }
    }

    public static final class OptionSet {
        public static final String CUST_TYPE_GROUP_TYPE = "CUST_TYPE_GROUP_TYPE";
        public static final String VAS_EXCLUSIVE_GROUP = "VAS_EXCLUSIVE_GROUP";

        public static final String VIEW_PRODUCT_GROUP_DIDONG = "VIEW_PRODUCT_GROUP_DIDONG";

        private OptionSet() {
        }
    }

    public static final class ActionCode {
        /** Mã hành động mặc định (đấu nối thuê bao) khi actionCode truyền vào rỗng. */
        public static final String SUB_CONNECTION = "00";

        private ActionCode() {
        }
    }

    public static final class Status {
        public static final String ACTIVE = "1";
        public static final String INACTIVE = "0";

        private Status() {
        }
    }

    public static final class SpecCharType {
        /** Loại sản phẩm price plan (product_offer_type_id=200) — xác định qua đối chiếu dữ liệu thật. */
        public static final Long OFFERING = 200L;

        private SpecCharType() {
        }
    }

    public static final class Condition {
        public static final String AND = "AND";
        public static final String OR = "OR";

        private Condition() {
        }
    }

    public static final class CharType {
        public static final String PRICE_PLAN = "2";

        private CharType() {
        }
    }

    public static final class ProductOfferType {
        public static final Long VAS = 300L;
        /** Loại sản phẩm mặc định (product code thường) khi không truyền productOfferType. */
        public static final Long PRODUCT_CODE = 200L;

        private ProductOfferType() {
        }
    }

    public static final class RelationType {
        /** Quan hệ "sản phẩm chính dùng VAS" — xác định qua dữ liệu thật: 6580/6601 (99.7%)
         * bản ghi PRODUCT_OFFER_RELATION trỏ tới offer VAS (product_offer_type_id=300) dùng giá trị này. */
        public static final Long VAS = 4L;

        private RelationType() {
        }
    }

    public static final class SubType {
        /** Xác định qua đối chiếu dữ liệu thật với các mã VAS trả trước đã biết (GPRS0, GPRS5, MBI1xx, G1FRPR,
          * D3U300). */
        public static final String PRE = "2";
        /** Xác định qua đối chiếu dữ liệu thật với các mã VAS trả sau đã biết (GPR_D0, GP_D10, G_D300, POSTG1). */
        public static final String POST = "1";

        private SubType() {
        }
    }

    public static final class ProductPackageType {
        public static final String PACKAGE_GOODS = "1";
        public static final String SALE_SERVICE = "2";
        public static final int MAX_LENGTH_CODE = 50;
        public static final int MAX_LENGTH_NAME = 500;
        public static final int MAX_LENGTH_DESC = 512;
        public static final String PRODUCT_PACKAGE_CODE_REGEX = "^[a-zA-Z0-9_]+$";
        public static final String INVOICE_DECLARATION = "INVOICE_DECLARATION";
        public static final String SALE_SERVICE_COMBO_NAME = "SALE_SERVICE_COMBO_NAME";

        private ProductPackageType() {
        }
    }
}
