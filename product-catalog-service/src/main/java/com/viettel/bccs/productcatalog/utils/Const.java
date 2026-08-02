package com.viettel.bccs.productcatalog.utils;

public class Const {
    public static final String DEFAULT_PRODUCT_SCHEMA = "BCCS_PRODUCT.";

    public static final class REST_RESPONSE_CODE {
        public static final String SUCCESS = "000";
        public static final String DATA_NOT_FOUND = "1246";
        public static final String ERROR = "999";

        private REST_RESPONSE_CODE() {}
    }

    public static final class OPTION_SET {
        public static final String CUST_TYPE_GROUP_TYPE = "CUST_TYPE_GROUP_TYPE";

        private OPTION_SET() {}
    }

    public static final class STATUS {
        public static final String ACTIVE = "1";

        private STATUS() {}
    }
    public final static class PRODUCT_PACKAGE_TYPE {
        public final static String PACKAGE_GOODS = "1";
        public final static String SALE_SERVICE = "2";
        public final static int MAX_LENGTH_CODE = 50;
        public final static int MAX_LENGTH_NAME = 500;
        public final static int MAX_LENGTH_DESC = 512;
        public final static String PRODUCT_PACKAGE_CODE_REGEX = "^[a-zA-Z0-9_]+$";
        public final static String INVOICE_DECLARATION = "INVOICE_DECLARATION";
        public final static String SALE_SERVICE_COMBO_NAME = "SALE_SERVICE_COMBO_NAME";


        private PRODUCT_PACKAGE_TYPE() {
        }
    }
}
