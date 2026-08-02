-- auto-generated definition
create table CHANNEL_TYPE
(
    CHANNEL_TYPE_ID         NUMBER(10)              not null
        constraint CHANNEL_TYPE_PK
            primary key,
    NAME                    VARCHAR2(50)            not null,
    STATUS                  NUMBER(1)   default '1' not null,
    OBJECT_TYPE             VARCHAR2(1) default '1',
    IS_VT_UNIT              VARCHAR2(1) default '1',
    CHECK_COMM              VARCHAR2(1) default '2',
    STOCK_TYPE              NUMBER(1)   default '1',
    STOCK_REPORT_TEMPLATE   VARCHAR2(50),
    TOTAL_DEBIT             NUMBER(10)  default 0,
    ALLOW_ADD_BATCH         NUMBER(1),
    SUFFIX_OBJECT_CODE      VARCHAR2(50),
    UPDATE_STAFF_OWNER_ROLE VARCHAR2(100),
    DISCOUNT_POLICY_DEFAUT  VARCHAR2(100),
    PRICE_POLICY_DEFAUT     VARCHAR2(100),
    UPDATE_BLANK_CODE_ROLE  VARCHAR2(100),
    UPDATE_OBJECT_INFO_ROLE VARCHAR2(100),
    UPDATE_SHOP_ROLE        VARCHAR2(100),
    CODE                    VARCHAR2(50),
    GROUP_CHANNEL_TYPE_ID   NUMBER(10),
    GROUP_CHANNEL_ID        NUMBER(10),
    IS_COLL_CHANNEL         NUMBER(2),
    IS_NOT_BLANK_CODE       NUMBER(2),
    IS_VHR_CHANNEL          NUMBER(2),
    CREATE_DATETIME         DATE,
    CREATE_USER             VARCHAR2(100),
    UPDATE_USER             VARCHAR2(50),
    UPDATE_DATETIME         DATE,
    PAYMENT_CODE            VARCHAR2(100),
    PAYMENT_TAIL            VARCHAR2(100),
    ASSIGN_CUST_STATUS      NUMBER(1),
    DESCRIPTION             NVARCHAR2(1000)
)
/

comment on table CHANNEL_TYPE is 'Danh mục kênh bán hàng, cửa hàng, kênh, nhân viên, CTV ...'
/

comment on column CHANNEL_TYPE.STATUS is '0: Khong hieu luc, 1: Hieu luc'
/

comment on column CHANNEL_TYPE.OBJECT_TYPE is '1: shop, 2: staff'
/

comment on column CHANNEL_TYPE.IS_VT_UNIT is '1: Thuoc VT, 2 khong thuoc viettel'
/

comment on column CHANNEL_TYPE.CHECK_COMM is '1: tinh hoa hong, 2 khong tinh hoa'
/

comment on column CHANNEL_TYPE.STOCK_TYPE is 'Loai kho thao tac, 1: shop, 2: staff, bo, khong SD'
/

comment on column CHANNEL_TYPE.STOCK_REPORT_TEMPLATE is 'Mau phieu, lenh xuat nhap kho'
/

comment on column CHANNEL_TYPE.TOTAL_DEBIT is 'So lan dat coc toi da'
/

comment on column CHANNEL_TYPE.GROUP_CHANNEL_TYPE_ID is 'Id loai nhom kenh,ref: APP_PARAMS.TYPE=channel_group_type'
/

comment on column CHANNEL_TYPE.GROUP_CHANNEL_ID is 'Id nhom kenh, ref: CHANNEL_GROUP.channel_group_id'
/

comment on column CHANNEL_TYPE.IS_NOT_BLANK_CODE is 'Xac dinh khong tao ma trang: 1: Khong tao ma trang; Nguoc lai: Tao ma trang'
/

comment on column CHANNEL_TYPE.IS_VHR_CHANNEL is 'Truong xac dinh lay thong tin VHR: 1: Lay tu VHR; Nguoc lai khong lay thong tin'
/

comment on column CHANNEL_TYPE.ASSIGN_CUST_STATUS is '1: Có giao, 2: không giao'
/