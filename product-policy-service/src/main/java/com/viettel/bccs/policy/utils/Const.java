package com.viettel.bccs.policy.utils;

public final class Const {
    public static final String STRING_DESCRIPTION_REASON_COMMITMENT = "$PhatCamKet$";
    public static final String ATTRIBUTE_PARAM_SPLIT = ",";
    public static final String DEFAULT_VALUE_MAP_SELECT_ALL = "-1";
    public static final String DEFAULT_PRODUCT_SCHEMA = "BCCS_PRODUCT.";
    public static final String VAS_NOT_FOUND_ERROR_CODE = "VAS_NOT_FOUND_ERROR_CODE";
    public static final String VAS_MAPPING_INVALID = "VAS_MAPPING_INVALID";
    public static final String DEFAULT_ALL = "-1";


    public static final class ActionCode {
        public static final String SUB_CONNECTION = "00";

        private ActionCode() {
        }
    }

    public static final class PrioritizeCode {
        public static final String TOM690_12 = "TOM690_12";
        public static final String POBAS_BASIC = "POBAS_BASIC";

        private PrioritizeCode() {
        }
    }

    public static final class Status {
        public static final String ACTIVE = "1";

        private Status() {
        }
    }

    public static final class StaffExtKey {
        public static final String MAP_AREA_CHAIN_CHANNEL = "MAP_AREA_CHAIN_CHANNEL";
        public static final String BUSINESS_SPEC = "BUSINESS_SPEC";
        public static final String BUSINESS_TYPE_STAFF = "BUSINESS_TYPE_STAFF";

        private StaffExtKey() {
        }
    }

    private Const() {
    }

    public static final class PayType {
        public static final String POSTPAID = "1"; //tra sau
        public static final String PREPAID = "2";  //tra truoc

        private PayType() {
        }
    }

    public static final class ProductOfferType {
        public static final Long ACCESSORIES = 10L;
        public static final String GOODS = "100";
        public static final String PRODUCT_CODE = "200";
        public static final String VAS = "300";
        public static final Long MIN_NUMBER_OFFER = 0L;
        public static final Long MAX_NUMBER_OFFER = 1000L;
        public static final Long STOCK_HANDSET = 7L;
        public static final Long NO_SERIAL = 11L;
        public static final Long CARD = 6L;
        public static final Long ISDN_MOBILE = 1L;
        public static final Long ISDN_HOMEPHONE = 2L;
        public static final Long ISDN_PSTN = 3L;
        public static final Long SIM = 4L;
        public static final Long PRODUCT_GOOD_LIMIT = 11L;
        public static final Long SME_PRODUCT_OFFERING = 14L;
        public static final Long SME_PRODUCT_OFFERING_VAS = 15L;

        private ProductOfferType() {
        }
    }

    public static final class OptionSet {
        public static final String ACTION_REQUIRE_MAP_ACTIVE_INFO = "ACTION_REQUIRE_MAP_ACTIVE_INFO";
        public static final String ACTION_REQUIRE_MAP_ACTIVE_INFO_FOR_VAS = "ACTION_REQUIRE_MAP_ACTIVE_INFO_FOR_VAS";
        public static final String CUSTOM_ACTION_WITH_SERVICE = "CUSTOM_ACTION_WITH_SERVICE";
        public static final String CHECK_MAP_BUSINESS_PRODUCT = "CHECK_MAP_BUSINESS_PRODUCT";
        public static final String CONFIG_MAPPING_BY_USER_AREA = "CONFIG_MAPPING_BY_USER_AREA";
        public static final String CHECK_MAPPING_BY_USER_AREA = "CHECK_MAPPING_BY_USER_AREA";

        public static final String OFFER_FILTER_MODE = "OFFER_FILTER_MODE";
        public static final String ACTION_CODE_ALLOW_OFFER_FILTER_MODE = "ACTION_CODE_ALLOW_OFFER_FILTER_MODE";

        public static final String LIST_PRIORITY_OFFER = "LIST_PRIORITY_OFFER";
        public static final String MY_VIETTEL_USER = "MY_VIETTEL_USER";

        private OptionSet() {
        }
    }

    public static final class ProductPackage {
        public static final String EXCLUDE_PROD_OFFER_TYPE_ID = "EXCLUDE_PROD_OFFER_TYPE_ID";

        private ProductPackage() {
        }
    }

    // VsaleRole constants cho phan quyen goi cuoc vsale
    public static final class VsaleRole {
        // TODO: Xac nhan gia tri role M2M tu nguoi dung/DB
        public static final String VSALE_DAUNOI_DIDONG_GOIM2M = "VSALE_DAUNOI_DIDONG_GOIM2M";
        // TODO: Xac nhan gia tri role goi dac biet tu nguoi dung/DB
        public static final String VSALE_DAUNOI_DIDONG_GOIDACBIET = "VSALE_DAUNOI_DIDONG_GOIDACBIET";
        // TODO: Xac nhan gia tri role goi thuong tu nguoi dung/DB
        public static final String VSALE_DAUNOI_DIDONG_GOITHUONG = "VSALE_DAUNOI_DIDONG_GOITHUONG";

        private VsaleRole() {
        }
    }

    public static final class ProductSpecChar {

        public static final String CUST_TYPE = "CUST_TYPE";
        public static final String LIST_VAS_BUNDLE = "LIST_VAS_BUNDLE";
        public static final String PREPAID_DURATION = "PREPAID_DURATION";
        public static final String CONNECT_ON_DIGITAL = "CONNECT_ON_DIGITAL";
        public static final String CONSUMERS = "CONSUMERS";
        public static final String SHOW_CB = "SHOW_CB";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String TIVI1 = "TIVI1";
        public static final String TIVI2 = "TIVI2";
        public static final String GIN_GIU_THUE_BAO = "GIN_GIU_THUE_BAO";
        public static final String NUM_3G_DEVICE = "NUM_3G_DEVICE";
        public static final String PROM_FEE = "PROM_FEE";
        public static final String PROM_LEVEL = "PROM_LEVEL";
        public static final String ACCESSORIES = "ACCESSORIES";
        public static final String CHUYEN_CN_CD = "CHUYEN_CN_CD";
        public static final String DAUNOI_CD = "DAUNOI_CD";
        public static final String SUPPORT_STATIC_IP = "SUPPORT_STATIC_IP";
        public static final String UPLOAD_SPEED = "UPLOAD_SPEED";
        public static final String GOODS_SERIAL = "SERIAL";
        public static final String GOODS_TBTP1C = "TBTP1C";
        public static final String IS_SPECIAL_PRODUCT = "IS_SPECIAL_PRODUCT";
        public static final String PRICE_COST = "PRICE_COST";
        public static final Long VALUE_SET_TYPE_DEFAULT = 0L;
        public static final Long VALUE_SET_TYPE_SQL = 1L;
        public static final Long VALUE_SET_TYPE_SOLR = 2L;
        public static final Long VALUE_SET_TYPE_WS = 3L;
        public static final Long VALUE_SET_TYPE_REST = 4L;
        public static final String VAS_SAFE_NET = "VAS_SAFE_NET";
        public static final String LISTING_PRICE = "LISTING_PRICE";
        public static final String ONT = "ONT";
        public static final String INFRATYPE = "INFRATYPE";
        public static final String VOIP = "VOIP";
        public static final String SINGLE_OR_COMBO = "SINGLE_OR_COMBO";
        public static final String STB1C = "STB1C";
        public static final String STB2C = "STB2C";
        public static final String TB_ATA = "TB_ATA";
        public static final String DOWNLOAD_SPEED = "DOWNLOAD_SPEED";
        public static final String NUMBER_OF_CHANNEL = "NUMBER_OF_CHANNEL";
        public static final String PROM_PRIORITY = "PROM_PRIORITY";
        public static final String IS_PROJECT = "IS_PROJECT";
        public static final String PORT4 = "4PORT";
        public static final String PORT1 = "1PORT";
        public static final String TB_IPP = "TB_IPP";
        public static final String TB_SWITCH = "TB_SWITCH";
        public static final String IS_XGSPON = "IS_XGSPON";
        public static final String REASON_PAY_TYPE = "REASON_PAY_TYPE";
        public static final String PROM_CATEGORY = "PROM_CATEGORY";
        public static final String PROM_FUTURE = "PROM_FUTURE";
        public static final String QOS = "QOS";
        public static final String HANGSX = "HANGSX";
        public static final String FTTH_TYPE = "FTTH_TYPE";
        public static final String SUPERNET = "SUPERNET";
        public static final String MESH = "MESH";
        public static final String ARPU_SOSANH = "ARPU_SOSANH";
        public static final String F5G_AREA = "F5G_AREA";
        public static final String ARPU_SINGLE = "ARPU_SINGLE";
        public static final String GOI_CUOC_DAC_THU = "GOI_CUOC_DAC_THU";
        public static final String SPECIFIC_PACKAGE = "SPECIFIC_PACKAGE";
        public static final String PRODUCT_STUDENT = "PRODUCT_STUDENT";
        public static final String STU_PRO = "STU_PRO";
        public static final String SPEC_HISCL = "SPEC_HISCL";
        public static final String TRATRUOC = "TRATRUOC";
        public static final String HOME_WIFI = "HOME_WIFI";
        public static final String FTTH_PRODUCT_CODE = "FTTH_PRODUCT_CODE";
        public static final String FTTH_OLD_CODE = "FTTH_OLD_CODE";

        private ProductSpecChar() {
        }
    }

    // MdealerRole constants
    public static final class MdealerRole {
        // TODO: Xac nhan gia tri role MDealer goi thuong tu nguoi dung/DB
        public static final String MDEALER_DAUNOI_DIDONG_GOITHUONG = "MDEALER_DAUNOI_DIDONG_GOITHUONG";

        private MdealerRole() {
        }
    }

    public static final class TelecomServiceId {
        public static final Long MOBILE = 1L;
        public static final Long HOMEPHONE = 2L;
        public static final Long CABLE_TV = 19L;
        public static final Long MULTI_SCREEN_1C = 35L;
        public static final Long MULTI_SCREEN_2C = 45L;
        public static final Long SMAS = 21L;
        public static final Long SMSPARENT = 22L;
        public static final Long INTERNET_EOC = 25L;
        public static final Long OTT_PRE = 56L;
        public static final Long V_TRACKING = 24L;
        public static final Long TV360_BOXTV_TT = 152L;
        public static final Long CAMERA = 39L;
        public static final Long TV360_BOXTV_TS = 153L;
        public static final Long IBC = 241L;
        public static final Long OTM = 254L;

        public static final Long DEFAULT_VALUE_MAP_SELECT_ALL = -1L;

        public static final Long ID_HOME_PHONE = 2L;
        public static final Long PSTN = 3L;
        public static final Long ADSL = 4L;
        public static final Long LEASEDLINE = 5L;
        public static final Long WHITE_LEASEDLINE = 12L;
        public static final Long OFFICE_WAN = 11L;
        public static final Long WHITE_LL_QT = 30L;
        public static final Long METRO_WAN = 29L;
        public static final Long NGN = 23L;
        public static final Long TRUNK = 31L;
        public static final Long PSTN_1800 = 32L;
        public static final Long PSTN_1900 = 33L;
        public static final Long IPTV = 9L;
        public static final Long FTTH = 28L;
        public static final Long NEXT = 9L;
        public static final Long PPPOE = 23L;

        public static final String POST_PAID = "1";
        public static final String PRE_PAID = "2";
        public static final String SERVICE_ALIAS_MOBILE = "M";
        public static final String SERVICE_ALIAS_HOMEPHONE = "H";
        public static final String SERVICE_ALIAS_PSTN = "P";
        public static final String SERVICE_ALIAS_ADSL = "A";
        public static final String SERVICE_ALIAS_SMAS = "S";
        public static final String SERVICE_ALIAS_SMS_PARENT = "R";
        public static final String SERVICE_ALIAS_V_TRACKING = "B";

        private TelecomServiceId() {
        }
    }

    public static final class ChannelType {
        public static final Long CHANNEL_COMPONENT_IM = 1L;

        public static final String IS_VT_UNIT = "1"; //thuoc viettel
        public static final String IS_NOT_VT_UNIT = "2"; //Khong thuoc viettel
        public static final String OBJECT_TYPE_SHOP = "1"; //Cua hang dai ly
        public static final String OBJECT_TYPE_STAFF = "2"; //Chi nhanh
        public static final Long ASSIGN_CUST_STATUS_TRANS = 1L;
        public static final Long ASSIGN_CUST_STATUS_NO_TRANS = 0L;

        public static final Long CHANNEL_TYPE_SALE = 1L;// Kenh ban hang
        public static final Long CHANNEL_TYPE_NV = 14L;// kenh nhan vien
        public static final Long CHANNEL_TYPE_NVDB = 10L;// kenh nhan vien diem ban
        // decode lai neu la diem ban ma co point of sale=1
        public static final Long CHANNEL_TYPE_DECODE_DB_POINT_OF_SALE = 80043L;
        // decode lai neu la nhan vien dia ban ma co point of sale=2
        public static final Long CHANNEL_TYPE_DECODE_NVDB_POINT_OF_SALE = 10L;
        public static final String POINT_OF_SALE_DB = "1";// kenh nhan diem ban
        public static final String POINT_OF_SALE_NVDB = "2";// kenh nhan dia ban
        public static final String CHANNEL_TYPE_CHECKCOM = "1";// co tinh hoa hong
        public static final String CHANNEL_TYPE_CHECKCOM_NO = "0";// khong tinh hoa hong
        public static final String CHANNEL_ROLE = "CHANNEL_ROLE";
        public static final String CHANNEL_GROUP_TYPE = "CHANNEL_GROUP_TYPE";
        public static final String DISCOUNT_POLICY = "DISCOUNT_POLICY";
        public static final String IS_VHR = "1";
        public static final String IS_NOT_VHR = "0";
        public static final Long CHANNEL_TYPE_HKD = 1001527L; //ma kenh ho kinh doanh
        public static final Long CHANNEL_DL_DGD_XA = 37L; //ma kenh DL DGD xa
        public static final Long CHUOI_TOAN_QUOC = 1001526L; //kenh chuoi toan quoc
        public static final Long VTP_CTVBHS = 1001541L; //kenh chuoi toan quoc
        public static final Long VIETTEL_MONEY_XHH = 1000498L; //Kênh CTV Viettel Money_XHH
        public static final Long VIETTEL_MONEY_BHXH = 1001541L; //Kênh CTV Viettel Money_BHXH
        public static final Long VIETTEL_MONEY_EVN = 1001557L; //Kênh CTV Viettel Money_EVN
        public static final Long CHANNEL_1001549 = 1001549L; //xa hoi hoa vietel money
        public static final Long CHANNEL_1001548 = 1001548L; //kenh đại lý vietel money
        public static final Long CTV_DN_VIETTELPOST = 1001540L; //Kênh CTV tư vấn bán hàng DN
        public static final Long DTMG_KHDN = 1000495L; //Kênh Đối tác môi giới_KHDN
        public static final String CHANNEL_DLUQ = "5";
        public static final String CHANNEL_CHTT = "164";
        public static final String CHANNEL_CHUOI_TQ = "1001526";
        public static final String CHANNEL_DL_XNK = "6";
        public static final String CHANNEL_CHUOI_ST = "80";

        private ChannelType() {
        }
    }

    public static final class MapActiveInfo {
        public static final int MODE_1 = 1;//offerId,regReasonId,promCode
        public static final int MODE_2 = 2;//offerId,promCode,regReasonId
        public static final int MODE_3 = 3;//offerId,promCode,regReasonId

        public static final int FILTER_MODE_ALL = 0;
        public static final int FILTER_MODE_ONLY_INDIVIDUAL = 1;
        public static final int HYBRID = 2;
        public static final int MODE_6 = 6;

        public static final String[] orderFields1() {
            return orderFields1;
        }

        private static final String[] orderFields1 = new String[]{"payType", "actionCode", "telServiceId", "staffCode",
                "shopCode",
                "channelTypeId", "districtCode", "provinceCode", "precinctCode", "technology",
                "customerGroup", "customerType", "subGroup", "subType", "stationId",
                "offerId", "regReasonId", "promCode"};

        public static final int[] filterModes1() {
            return filterModes1;
        }

        private static final int[] filterModes1 = new int[]{FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL};

        public static final String[] nonMapFields() {
            return nonMapFields;
        }

        public static final int[] filterModes3() {
            return filterModes3;
        }

        public static final String STATION_CODES = "stationCodes";
        private static final String[] nonMapFields = new String[]{STATION_CODES};
        private static final int[] filterModes3 = new int[]{FILTER_MODE_ONLY_INDIVIDUAL};


        //phuc vu kieu moi tim theo ElasticSearch hoac DB
        public static final String[] nonElasticSearchMapFields1() {
            return nonElasticSearchMapFields1;
        }

        public static final int[] nonElasticSearchFilterModes1() {
            return nonElasticSearchFilterModes1;
        }

        private static final String[] nonElasticSearchMapFields1 = new String[]{"offerId", "regReasonId", "promCode"};
        private static final int[] nonElasticSearchFilterModes1 = new int[]{FILTER_MODE_ONLY_INDIVIDUAL,
                FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL};

        private static final String[] nonElasticSearchMapFields3 = new String[]{"offerId", "promCode", "regReasonId"};
        private static final int[] nonElasticSearchFilterModes3 = new int[]{FILTER_MODE_ONLY_INDIVIDUAL,
                HYBRID, FILTER_MODE_ONLY_INDIVIDUAL};

        public static final String[] nonElasticSearchMapFields2() {
            return nonElasticSearchMapFields2;
        }

        public static final String[] nonElasticSearchMapFields3() {
            return nonElasticSearchMapFields3;
        }

        public static final int[] nonElasticSearchFilterModes2() {
            return nonElasticSearchFilterModes2;
        }

        public static final int[] nonElasticSearchFilterModes3() {
            return nonElasticSearchFilterModes3;
        }

        private static final String[] nonElasticSearchMapFields2 = new String[]{"offerId", "promCode", "regReasonId"};
        private static final int[] nonElasticSearchFilterModes2 = new int[]{FILTER_MODE_ONLY_INDIVIDUAL,
                FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL};

        private static final String[] orderFields2 = new String[]{"payType", "actionCode", "telServiceId", "staffCode",
                "shopCode",
                "channelTypeId", "districtCode", "provinceCode", "precinctCode", "technology",
                "customerGroup", "customerType", "subGroup", "subType", "stationId",
                "offerId", "promCode", "regReasonId"};

        public static final String[] orderFields2() {
            return orderFields2;
        }

        private static final int[] filterModes2 = new int[]{FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL, FILTER_MODE_ALL,
                FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL};

        public static final int[] filterModes2() {
            return filterModes2;
        }

        private static final String[] orderFields6 = new String[]{"nodeCode", "precinctCode", "stationCodes",
                "districtCode", "provinceCode"};

        public static final String[] orderFields6() {
            return orderFields6;
        }

        private static final int[] filterModes6 = new int[]{FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL,
                FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL, FILTER_MODE_ONLY_INDIVIDUAL};

        public static final int[] filterModes6() {
            return filterModes6;
        }
    }
}