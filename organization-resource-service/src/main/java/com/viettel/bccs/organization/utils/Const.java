package com.viettel.bccs.organization.utils;

public class Const {
    public static final String OPTION_SET_CODE_CVS_STOCK_ISDN_VSALE = "CTV_STOCK_ISDN_VSALE";
    public static final String OPTION_SET_CODE_LIST_IDENTITY_GROUPTYPE_ = "LIST_IDENTITY_GROUPTYPE_";

    public static final class ShopService {
        public static final int OWNER_TYPE_SHOP = 1;
        public static final int OWNER_TYPE_STAFF = 2;
        public static final String SHOP_KEY_PREFIX = "SHOP_";
        public static final String STAFF_KEY_PREFIX = "STAFF_";
    }

    public static final class STAFF_EXT_KEY {
        public static final String BUSINESS_SPEC = "BUSINESS_SPEC";
        public static final String BUSINESS_TYPE_STAFF = "BUSINESS_TYPE_STAFF";
    }

    public static final class STATUS {
        public static final String ACTIVE = "1";
    }

    public static final class CHANNEL_TYPE {
        public static final String IS_NOT_VT_UNIT = "2"; // Khong thuoc Viettel
        public static final String OBJECT_TYPE_STAFF = "2"; // Chi nhanh
        public static final String OBJECT_TYPE_SHOP = "1"; // Cửa hàng
        public static final Long STOCK_FUNCTIONAL_CHANNEL = 8L; // Loai kenh "Kho chuc nang rieng"
    }
}
