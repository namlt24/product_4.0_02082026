package com.viettel.bccs.productcatalog.utils;

public class ErrorCode {


    public final class ErrorUser {

        public static final String ERROR_USER_PASS_INVALID = "100"; // Loi user mat khau khong hop le
        public static final String ERROR_USER_PERMISSION = "101"; // Loi khong du tham quyen truy cap
        public static final String ERROR_USER_INVALID_FORMAT = "102"; // Loi khong dung dinh dang, loi validate
        public static final String ERROR_USER_REQUIRE = "103"; // Loi thong tin bat buoc nhap
        public static final String ERROR_USER_DUPLICATE_OBJECT = "104"; // Loi duplicate du lieu
        public static final String ERROR_USER_OBJECT_IS_EMPTY = "105"; // Loi du lieu rong
    }


    public final class ErrorConnnect {
        public static final String ERROR_CONNECT_EX = "501";
        public static final String ERROR_TIMOUT_EX = "503";
        public static final String ERROR_SYSTEM_PROVISIONING = "504";//Loi giao tiep Provisioning
        public static final String ERROR_SYSTEM_INVENTORY = "505";//Loi giao tiep Inventory
        public static final String ERROR_SYSTEM_PRODUCT = "506";//Loi giao tiep Product
        public static final String ERROR_SYSTEM_PAYMENT = "507";//Loi giao tiep Payment
        public static final String ERROR_SYSTEM_BILLING = "508";//Loi giao tiep Billiing
        public static final String ERROR_SYSTEM_NIMS = "509";//Loi giao tiep NIMS
        public static final String ERROR_SYSTEM_QLCTKT = "510";//Loi giao tiep QLCTKT
        public static final String ERROR_SYSTEM_VOFFICE = "511";//Loi giao tiep Voffice
        public static final String ERROR_SYSTEM_ERP = "512";//Loi giao tiep ERP tai chinh
        public static final String ERROR_SYSTEM_LOGISTIC = "513";//Loi giao tiep Logistic
        public static final String ERROR_SYSTEM_ROAMING = "514"; // Loi giao tiep Roaming
        public static final String ERROR_SYSTEM_PRIVILEGE = "515"; // Loi giao tiep PRIVILEGE
        public static final String ERROR_SYSTEM_HMSD_WS = "516"; // Loi giao tiep HMSD
        public static final String ERROR_SYSTEM_SALE = "517"; // Loi giao tiep HMSD
        public static final String ERROR_SYSTEM_CC = "518"; // Loi giao tiep CC
        public static final String ERROR_SYSTEM_KTTS = "519"; // Loi giao tiep voi he thong kho tang tai san
        public static final String ERROR_SYSTEM_PBH = "520"; // Loi giao tiep voi he thong phi ban hang
        public static final String ERROR_SYSTEM_CHATBOT = "521";// Loi giao tiep voi he thong chatbot
        // ma loi dau noi theu bao co dinh thanh cong nhung cham dut that bai
        public static final String ERROR_CONN_SUCCESS_TERMINATE_FAILED_SALE = "-18";
    }


    public final class ErrorStandard {
        public static final String ERROR_NULL_POINTER_EX = "200";
        public static final String ERROR_ARRAY_OUT_OF_BOUNDS_EX = "201";
        public static final String ERROR_CLASS_NOT_FOUND = "202";
        public static final String ERROR_NUMBER_FORMAT_EX = "203";
        public static final String ERROR_PARSE_EX = "204";
        public static final String ERROR_CLASS_CAST_EX = "205";
        public static final String ERROR_VALIDATE_INPUT = "206";
        public static final String ERROR_DUPLICATE = "1";
        public static final String SUCCESS = "0";
        public static final String ERROR_EXCEPTION = "2";
        public static final String ERROR_UPDATE = "207";
        public static final String ERROR_INSERT = "208";
        public static final String ERROR_DELETE = "209";
        public static final String ERROR_SAVE_OBJECT_DATABASE = "210";
        public static final String ERROR_INNER_VALIDATE = "211";
    }



    public final class ErrorMemory {

    }



    public final class ErrorHandleFile {
        public static final String ERROR_FILE_NOT_FOUND_EX = "600";

    }



    public final class ErrorDatabase {
        public static final String ERROR_DATA_NOT_FOUND = "300"; // Loi khong tim thay du lieu


    }

    public final class ErrorBusiness {
        public static final String ERROR_BUSSINESS_CONSTRAIN = "900";
        public static final String ERROR_BUSSINESS_EXISTED = "ERROR_BUSSINESS_EXISTED";
        public static final String ERROR_BUSSINESS_NOT_EXISTED = "ERROR_BUSSINESS_NOT_EXISTED";
    }

    public static final String ERROR_NOT_DEFINE = "";

    public static final String ID_SUCCESS = "000001";
    public static final String SUCCESSFUL = "000";


    public final class ErrorInvoice {
        public static final String LACK_OF_INVOICE = "LACK_OF_INVOICE";
        public static final String CAN_NOT_GET_INVOICE = "CAN_NOT_GET_INVOICE";
    }

    public final class ErrorCheckSaleTrans {
        public static final String ERROR_PAYMENT_DEBIT_SALE_TRANS = "EU1021100104";
        public static final String ERROR_PAYMENT_DEBIT_SALE_TRANS_BANKPLUS = "EU1021100103";
        public static final String ERROR_PAYMENT_DEBIT_SALE_TRANS_BANKPLUS_PREPAID = "EU10211001031";
    }

    public static final String DUPLICATE_BANKPLUS = "405";//gmoz PYC_3590977 Quanvv 25122020 add
    public static final String DUPLICATE_BRANDNAME = "406";
    public static final String EMPTY_BRANDNAME = "404";
    public static final String CANCEL_BRANDNAME = "403";

    public final class ErrorWsCreateChannelRequest {
        public static final String SUCCESS = "0"; // lap y/c thanh cong
        public static final String ERROR_PRODUCT = "1"; // loi tren ht product
        public static final String ERROR_PROFILE = "2"; // loi tren ht ho so
        public static final String ERROR_ORDER = "3"; // loi tren ht order
        public static final String SUCCESS_VO = "4"; // trinh ky thanh cong
        public static final String ERROR_VO = "5"; // loi tren ht vo
        public static final String ERROR_HDDT = "7"; // loi hdtt
    }

}
