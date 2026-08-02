package com.viettel.bccs.productcatalog.utils;

public class ErrorCode {


    public final class ERROR_USER {

        public final static String ERROR_USER_PASS_INVALID = "100"; // Loi user mat khau khong hop le
        public final static String ERROR_USER_PERMISSION = "101"; // Loi khong du tham quyen truy cap
        public final static String ERROR_USER_INVALID_FORMAT = "102"; // Loi khong dung dinh dang, loi validate
        public final static String ERROR_USER_REQUIRE = "103"; // Loi thong tin bat buoc nhap
        public final static String ERROR_USER_DUPLICATE_OBJECT = "104"; // Loi duplicate du lieu
        public final static String ERROR_USER_OBJECT_IS_EMPTY = "105"; // Loi du lieu rong
    }


    public final class ERROR_CONNNECT {
        public final static String ERROR_CONNECT_EX = "501";
        public final static String ERROR_TIMOUT_EX = "503";
        public final static String ERROR_SYSTEM_PROVISIONING = "504";//Loi giao tiep Provisioning
        public final static String ERROR_SYSTEM_INVENTORY = "505";//Loi giao tiep Inventory
        public final static String ERROR_SYSTEM_PRODUCT = "506";//Loi giao tiep Product
        public final static String ERROR_SYSTEM_PAYMENT = "507";//Loi giao tiep Payment
        public final static String ERROR_SYSTEM_BILLING = "508";//Loi giao tiep Billiing
        public final static String ERROR_SYSTEM_NIMS = "509";//Loi giao tiep NIMS
        public final static String ERROR_SYSTEM_QLCTKT = "510";//Loi giao tiep QLCTKT
        public final static String ERROR_SYSTEM_VOFFICE = "511";//Loi giao tiep Voffice
        public final static String ERROR_SYSTEM_ERP = "512";//Loi giao tiep ERP tai chinh
        public final static String ERROR_SYSTEM_LOGISTIC = "513";//Loi giao tiep Logistic
        public final static String ERROR_SYSTEM_ROAMING = "514"; // Loi giao tiep Roaming
        public final static String ERROR_SYSTEM_PRIVILEGE = "515"; // Loi giao tiep PRIVILEGE
        public final static String ERROR_SYSTEM_HMSD_WS = "516"; // Loi giao tiep HMSD
        public final static String ERROR_SYSTEM_SALE = "517"; // Loi giao tiep HMSD
        public final static String ERROR_SYSTEM_CC = "518"; // Loi giao tiep CC
        public final static String ERROR_SYSTEM_KTTS = "519"; // Loi giao tiep voi he thong kho tang tai san
        public final static String ERROR_SYSTEM_PBH = "520"; // Loi giao tiep voi he thong phi ban hang
        public final static String ERROR_SYSTEM_CHATBOT = "521";// Loi giao tiep voi he thong chatbot
        public final static String ERROR_CONN_SUCCESS_TERMINATE_FAILED_SALE = "-18"; // ma loi dau noi theu bao co dinh thanh cong nhung cham dut that bai
    }


    public final class ERROR_STANDARD {
        public final static String ERROR_NULL_POINTER_EX = "200";
        public final static String ERROR_ARRAY_OUT_OF_BOUNDS_EX = "201";
        public final static String ERROR_CLASS_NOT_FOUND = "202";
        public final static String ERROR_NUMBER_FORMAT_EX = "203";
        public final static String ERROR_PARSE_EX = "204";
        public final static String ERROR_CLASS_CAST_EX = "205";
        public final static String ERROR_VALIDATE_INPUT = "206";
        public final static String ERROR_DUPLICATE = "1";
        public final static String SUCCESS = "0";
        public final static String ERROR_EXCEPTION = "2";
        public final static String ERROR_UPDATE = "207";
        public final static String ERROR_INSERT = "208";
        public final static String ERROR_DELETE = "209";
        public final static String ERROR_SAVE_OBJECT_DATABASE = "210";
        public final static String ERROR_INNER_VALIDATE = "211";
    }



    public final class ERROR_MEMORY {

    }



    public final class ERROR_HANDLE_FILE {
        public final static String ERROR_FILE_NOT_FOUND_EX = "600";

    }



    public final class ERROR_DATABASE {
        public final static String ERROR_DATA_NOT_FOUND = "300"; // Loi khong tim thay du lieu


    }

    public final class ERROR_BUSINESS {
        public final static String ERROR_BUSSINESS_CONSTRAIN = "900";
        public final static String ERROR_BUSSINESS_EXISTED = "ERROR_BUSSINESS_EXISTED";
        public final static String ERROR_BUSSINESS_NOT_EXISTED = "ERROR_BUSSINESS_NOT_EXISTED";
    }

    public final static String ERROR_NOT_DEFINE = "";

    public final static String ID_SUCCESS = "000001";
    public final static String SUCCESSFUL = "000";


    public final class ERROR_INVOICE {
        public final static String LACK_OF_INVOICE = "LACK_OF_INVOICE";
        public final static String CAN_NOT_GET_INVOICE = "CAN_NOT_GET_INVOICE";
    }

    public final class ERROR_CHECK_SALE_TRANS {
        public final static String ERROR_PAYMENT_DEBIT_SALE_TRANS = "EU1021100104";
        public final static String ERROR_PAYMENT_DEBIT_SALE_TRANS_BANKPLUS = "EU1021100103";
        public final static String ERROR_PAYMENT_DEBIT_SALE_TRANS_BANKPLUS_PREPAID = "EU10211001031";
    }

    public final static String DUPLICATE_BANKPLUS = "405";//gmoz PYC_3590977 Quanvv 25122020 add
    public final static String DUPLICATE_BRANDNAME = "406";
    public final static String EMPTY_BRANDNAME = "404";
    public final static String CANCEL_BRANDNAME = "403";

    public final class ERROR_WS_CREATE_CHANNEL_REQUEST {
        public final static String SUCCESS = "0"; // lap y/c thanh cong
        public final static String ERROR_PRODUCT = "1"; // loi tren ht product
        public final static String ERROR_PROFILE = "2"; // loi tren ht ho so
        public final static String ERROR_ORDER = "3"; // loi tren ht order
        public final static String SUCCESS_VO = "4"; // trinh ky thanh cong
        public final static String ERROR_VO = "5"; // loi tren ht vo
        public final static String ERROR_HDDT = "7"; // loi hdtt
    }

}
