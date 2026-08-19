-- Mapping "Kho so chuc nang" (SHOP co CHANNEL_TYPE_ID=8, "Kho chuc nang rieng") voi
-- Loai kenh > Cua hang > User (quan he N-N, 3 muc phan cap tuy chon).
-- Xem yeu cau: dactayeucau muc 3.3.4 "Xay dung chuc nang mapping Loai kenh voi kho chuc nang".
--
-- Muc phan cap xac dinh boi SHOP_ID/STAFF_ID:
--   - SHOP_ID = NULL, STAFF_ID = NULL  -> mapping muc kenh (kho gan cho ca Loai kenh)
--   - SHOP_ID co gia tri, STAFF_ID = NULL -> mapping muc cua hang
--   - SHOP_ID co gia tri, STAFF_ID co gia tri -> mapping muc user
--
-- Validate nghiep vu (thuc hien o tang service, KHONG phai rang buoc DB):
--   - Muc cua hang: SHOP.CHANNEL_TYPE_ID phai bang CHANNEL_TYPE_ID cua dong mapping (cua hang
--     phai thuoc kenh), va phai ton tai dong mapping muc kenh cung {CHANNEL_TYPE_ID, STOCK_SHOP_ID}
--     (kho phai da map cho kenh truoc khi map xuong cua hang).
--   - Muc user: STAFF.SHOP_ID phai bang SHOP_ID cua dong mapping (user phai thuoc cua hang), va
--     phai ton tai dong mapping muc cua hang cung {CHANNEL_TYPE_ID, SHOP_ID, STOCK_SHOP_ID}.
create table STOCK_CHANNEL_MAPPING
(
    STOCK_CHANNEL_MAPPING_ID NUMBER(10)  not null
        constraint PK_STOCK_CHANNEL_MAPPING
            primary key,
    TELECOM_SERVICE_ID       NUMBER(10)  not null,        -- Loai dich vu (ref TELECOM_SERVICE, cross-service product-catalog)
    CHANNEL_TYPE_ID          NUMBER(10)  not null,        -- Loai kenh (ref CHANNEL_TYPE.CHANNEL_TYPE_ID)
    STOCK_SHOP_ID            NUMBER(10)  not null,        -- Ma kho so chuc nang = SHOP.SHOP_ID voi CHANNEL_TYPE_ID=8
    SHOP_ID                  NUMBER(10),                  -- Ma cua hang (ref SHOP.SHOP_ID); NULL = mapping muc kenh
    STAFF_ID                 NUMBER(10),                  -- Ma user (ref STAFF.STAFF_ID); NULL = mapping muc kenh/cua hang
    EFFECT_DATE               DATE       not null,        -- Ngay hieu luc
    EXPIRE_DATE               DATE,                       -- Ngay het hieu luc (NULL = khong gioi han)
    STATUS                   VARCHAR2(1) not null,        -- '1' Hieu luc / '0' Khong hieu luc
    CREATE_USER              VARCHAR2(50),
    CREATE_DATETIME           DATE       not null,
    UPDATE_USER               VARCHAR2(50),
    UPDATE_DATETIME            DATE,
    constraint CK_STOCK_CHANNEL_MAP_HIER
        check (STAFF_ID IS NULL OR SHOP_ID IS NOT NULL)   -- co user thi bat buoc co cua hang
);

-- Index tra cuu/validate theo tung cot logic-FK
create index IDX_STOCK_CH_MAP_CHANNEL on STOCK_CHANNEL_MAPPING (CHANNEL_TYPE_ID);
create index IDX_STOCK_CH_MAP_STOCK on STOCK_CHANNEL_MAPPING (STOCK_SHOP_ID);
create index IDX_STOCK_CH_MAP_SHOP on STOCK_CHANNEL_MAPPING (SHOP_ID);
create index IDX_STOCK_CH_MAP_STAFF on STOCK_CHANNEL_MAPPING (STAFF_ID);
create index IDX_STOCK_CH_MAP_TELSVC on STOCK_CHANNEL_MAPPING (TELECOM_SERVICE_ID);

-- Index chinh cho truy van/nghiep vu chu dao: tim toan bo kho hien tai cua 1 nhom
-- {kenh, cua hang?, user?} - dung cho ca luong validate phan cap lan luong "reset danh sach
-- kho" khi tac dong = Update.
create index IDX_STOCK_CH_MAP_GROUP on STOCK_CHANNEL_MAPPING (CHANNEL_TYPE_ID, SHOP_ID, STAFF_ID, STOCK_SHOP_ID);
