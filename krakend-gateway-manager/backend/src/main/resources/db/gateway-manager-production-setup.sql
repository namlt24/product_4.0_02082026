-- ============================================================================
-- Gateway Manager - Script dung 1 lan de dung SCHEMA + DU LIEU THAT tren mot
-- Oracle production chua co 8 bang nay.
--
-- Nguon:
--   [DDL]  db/ddl-gateway-manager.sql - trich xuat THAT tu Oracle dev local
--          (DBMS_METADATA.GET_DDL) ngay 2026-08-27, da kiem tra chay sach
--          tren 1 schema trong (tao du 8 bang + 1 index, khong loi cu phap).
--   [DATA] gateway-manager-dump.sql nguoi dung cung cap (nguon that: Oracle
--          10.207.222.170:1521 DB170, schema BCCS_PRODUCT, xuat ngay
--          2026-08-27) - CHI phan du lieu THAT (4 Upstream Service + 2
--          Endpoint composite + 4 Step + 12 FieldMapping + 3 phien ban lich
--          su), KHONG gom du lieu demo/seed cua may dev (branch-demo,
--          canvas-demo... - nhung cai do do DataSeeder tu tao rieng tren
--          may dev, khong lien quan production).
--
-- 2 cho da SUA lai cho khop dung schema/enum HIEN TAI (khac ban dump goc):
--   1. Bo 2 cot CACHE_ENABLED/CACHE_TTL_SECONDS khoi INSERT UPSTREAM_SERVICE -
--      schema hien tai KHONG con 2 cot nay tren bang UPSTREAM_SERVICE (cache
--      da chuyen xuong cap BACKEND_STEP tu truoc, xem UpstreamService.java) -
--      insert nguyen ban se loi ORA-00904 invalid identifier.
--   2. Doi FIELD_MAPPING.SOURCE_TYPE tu 'REQUEST_QUERY' (ten dump goc dung,
--      co le dat boi 1 phien lam viec khac tren DB170) thanh 'QUERY_PARAM'
--      (dung ten enum FieldMappingSourceType.QUERY_PARAM hien tai trong code)
--      - sua ca trong cau INSERT FIELD_MAPPING lan trong JSON snapshot cua
--      ENDPOINT_CONFIG_VERSION #1 (tranh loi khi bam "rollback" ve version do
--      sau nay: JSON se khong deserialize duoc enum "REQUEST_QUERY" khong ton tai).
--   Da INSERT thu + verify THAT tren Oracle dev local (25/25 dong, goi lai
--   endpoint qua gateway thanh cong, chi loi timeout ket noi toi
--   10.207.252.17 vi may dev khong ra duoc mang noi bo do - khong phai loi
--   du lieu).
--
-- Cap nhat 2026-08-28 (lan 2): FIELD_MAPPING.CONSTANT_VALUE luc dau khong ghi ro
-- length tren @Column cua entity (JPA mac dinh 255) - ALTER TABLE tay len 4000 bi
-- Hibernate ddl-auto=update TU Y NOI LAI VE 255 o lan restart backend ke tiep (khop
-- theo dung entity). Da sua: ghi ro length=4000 tren entity + ALTER lai 4000 tren
-- DB dev - VARCHAR2(4000 CHAR) duoi day moi la gia tri DUNG/on dinh lau dai.
-- Cap nhat 2026-09-05 (lan 10): CHUYEN SANG FLYWAY cho instance MOI tu day -
-- backend/src/main/resources/db/migration/V1__baseline.sql (CHI 8 bang, KHONG
-- INSERT du lieu demo) la nguon chinh thuc cho 1 doi tu trien khai rieng tu bay
-- gio, KHONG con dung file nay chay tay nua (giu lam tai lieu lich su/tham khao).
-- Instance da co san du lieu (nhu duoi day) chuyen Flyway qua baseline-on-migrate,
-- khong anh huong gi. Da sua 2 cot bi Hibernate ddl-auto=update TU Y sinh sai
-- kieu BOOLEAN (chi Oracle 23c+, vo hieu tren 19c) thanh dung NUMBER(1,0):
-- BACKEND_STEP.CACHE_ENABLED, ENDPOINT_CONFIG.RESPONSE_CACHE_ENABLED.
-- Cap nhat 2026-09-03 (lan 9): them cot ENDPOINT_CONFIG.RESPONSE_CACHE_ENABLED +
-- RESPONSE_CACHE_TTL_SECONDS (dong bo voi ddl-gateway-manager.sql, mac dinh 0/300) -
-- cache TOAN BO response cho MOI client goi cung tham so, CHAN CUNG boi validate chi
-- bat duoc khi endpoint VA TOAN BO step deu la GET. Khong anh huong du lieu THAT o
-- tren (2 Endpoint composite hien co deu nhan mac dinh 0/300, dung y het hanh vi cu).
-- Cap nhat 2026-08-31 (lan 8): them cot UPSTREAM_SERVICE.MAX_CONCURRENT_CALLS +
-- MAX_WAIT_DURATION_MS (dong bo voi ddl-gateway-manager.sql, mac dinh 20/500 - dung
-- y het gia tri truoc day fix cung trong UpstreamHttpExecutor.bulkheadFor()) - cho
-- phep chinh rieng gioi han Bulkhead theo tung Upstream. Khong anh huong du lieu
-- THAT o tren (4 Upstream Service hien co deu nhan mac dinh 20/500, dung y het
-- hanh vi cu).
-- Cap nhat 2026-08-29 (lan 7): them cot BACKEND_STEP.COMPENSATION_UPSTREAM_SERVICE_ID/
-- COMPENSATION_METHOD/COMPENSATION_URL_PATTERN + FIELD_MAPPING.TARGET_CONTEXT (dong bo
-- voi ddl-gateway-manager.sql) - bu tru/rollback nghiep vu (saga best-effort, muc 6).
-- Khong anh huong du lieu THAT o tren, moi BACKEND_STEP/FIELD_MAPPING that hien co deu
-- nhan gia tri mac dinh (khong bu tru, TARGET_CONTEXT=MAIN).
--
-- Cap nhat 2026-08-29 (lan 6): them cot ENDPOINT_CONFIG.PARALLEL_EXECUTION (dong bo voi
-- ddl-gateway-manager.sql) - muc 4 (song song hoa THAT SU step doc lap qua thread pool).
-- Khong anh huong du lieu THAT o tren, moi ENDPOINT_CONFIG that hien co deu nhan gia tri
-- mac dinh 0 (tat song song, giu nguyen vong lap tuan tu cu).
--
-- Cap nhat 2026-08-29 (lan 7): them cot BACKEND_STEP.PARALLEL_GROUP (dong bo voi
-- ddl-gateway-manager.sql) - "wave" song song trong 1 chuoi sequential. Khong anh huong
-- du lieu THAT o tren, moi BACKEND_STEP that hien co deu PARALLEL_GROUP=NULL (chay tuan
-- tu binh thuong, khong bi anh huong).
--
-- Cap nhat 2026-08-28 (lan 5): them cot BACKEND_STEP.ON_ERROR_STEP_ORDER (dong bo voi
-- ddl-gateway-manager.sql) - khong anh huong du lieu THAT o tren, ca 4 BACKEND_STEP that
-- hien co deu ON_ERROR_STEP_ORDER=NULL (khong dung fallback loi).
--
-- Cap nhat 2026-08-28 (lan 4): them cot IDEMPOTENCY_ENABLED + IDEMPOTENCY_TTL_SECONDS vao
-- ENDPOINT_CONFIG (dong bo voi ddl-gateway-manager.sql) - khong anh huong du lieu THAT o
-- tren, ca 2 ENDPOINT_CONFIG that hien co deu nhan gia tri mac dinh (tat idempotency).
--
-- Cap nhat 2026-08-28 (lan 3): them 'STEP_RESPONSE_ARRAY_MERGE' vao CHECK constraint
-- cua FIELD_MAPPING.SOURCE_TYPE (dong bo voi ddl-gateway-manager.sql) - khong anh
-- huong du lieu THAT o tren, khong dung sourceType nay.
--
-- Cap nhat 2026-08-28 (lan 1): them 'CONSTANT' vao CHECK constraint cua
-- FIELD_MAPPING.SOURCE_TYPE + cot moi FIELD_MAPPING.CONSTANT_VALUE (dong bo voi
-- ddl-gateway-manager.sql) - khong anh huong du lieu THAT o tren, ca 12
-- FIELD_MAPPING that hien co deu khong dung sourceType=CONSTANT.
--
-- Cap nhat 2026-08-27: them 4 toan tu so sanh so (GREATER_THAN,
-- GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL) vao CHECK constraint
-- cua BACKEND_STEP.CONDITION_OPERATOR (dong bo voi ddl-gateway-manager.sql) -
-- khong anh huong du lieu THAT o tren, ca 4 BACKEND_STEP that hien co deu
-- CONDITION_OPERATOR=NULL (khong dung re nhanh).
--
-- HUONG DAN DUNG tren production:
--   1. sqlplus/SQLcl vao dung schema, SET DEFINE OFF truoc (URL_PATTERN co
--      ky tu "&", khong tat se bi hoi "Enter value for..." va hong cau lenh).
--   2. Chay het file nay 1 luot (DROP-neu-co truoc, DDL sau, INSERT cuoi,
--      dung thu tu FK). Idempotent: chay lai nhieu lan tren cung 1 schema
--      deu ra ket qua giong nhau (khong con bi loi "ORA-00955 name already
--      used" neu 8 bang da ton tai tu truoc).
--   3. Sau khi insert xong, app can duoc KHOI DONG LAI (hoac it nhat goi lai
--      /api/config/deploy CHO Endpoint - rieng Upstream Service can restart
--      that su, vi /deploy chi reload EndpointRegistryCache, KHONG reload
--      UpstreamRegistryCache - da xac nhan that khi verify tren dev local).
--
-- !!! CANH BAO QUAN TRONG - DOC TRUOC KHI CHAY TREN PRODUCTION THAT !!!
-- Buoc [0] ben duoi DROP (xoa han, KHONG the hoan tac) ca 8 bang neu chung
-- DA TON TAI tren schema dich - bao gom XOA SACH bat ky du lieu THAT nao
-- dang co san trong do (vd Endpoint/Upstream khac ngoai 2 cai duoc INSERT
-- lai o buoc [2]), khong chi rieng cau truc bang. CHI dung file nay neu ban
-- CHAC CHAN muon thay the toan bo 8 bang bang dung 25 dong du lieu o buoc
-- [2] - neu schema dich dang co du lieu PRODUCTION THAT khac can giu lai,
-- PHAI backup truoc (vd expdp/exp rieng 8 bang nay) hoac bo han buoc [0],
-- chi chay [1]+[2] tren schema con trong.
-- ============================================================================

SET DEFINE OFF

-- ============================================================================
-- [0] DROP neu da ton tai (idempotent - xem CANH BAO o tren truoc khi chay).
--     Thu tu NGUOC voi luc tao (bang con truoc, bang cha sau) + CASCADE
--     CONSTRAINTS de tu go moi FK tham chieu toi truoc khi xoa, PURGE de
--     khong giu ban ghi trong recyclebin (giai phong ten bang ngay lap tuc).
--     Bo qua an toan (khong bao loi) neu bang chua ton tai (ORA-00942).
-- ============================================================================

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "ENDPOINT_CONFIG_VERSION" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "FIELD_MAPPING" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "BACKEND_STEP_MAPPING" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "BACKEND_STEP_DENY" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "BACKEND_STEP_ALLOW" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "BACKEND_STEP" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "ENDPOINT_CONFIG" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "UPSTREAM_SERVICE" CASCADE CONSTRAINTS PURGE';
EXCEPTION
   WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

-- ============================================================================
-- [1] DDL - 8 bang
-- ============================================================================

CREATE TABLE "UPSTREAM_SERVICE"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"BASE_HOST" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CIRCUIT_BREAKER_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"CONNECT_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	"DESCRIPTION" VARCHAR2(255 CHAR),
	"FAILURE_RATE_THRESHOLD" NUMBER(10,0) NOT NULL ENABLE,
	"MAX_CONCURRENT_CALLS" NUMBER(10,0) DEFAULT 20 NOT NULL ENABLE,
	"MAX_WAIT_DURATION_MS" NUMBER(10,0) DEFAULT 500 NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"READ_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"RETRY_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"UPDATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	 CHECK (circuit_breaker_enabled in (0,1)) ENABLE,
	 CHECK (retry_enabled in (0,1)) ENABLE,
	 CONSTRAINT "UPSTREAM_SERVICE_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UKR9K2UNQ519ISGPHWC5EMCHYUC" UNIQUE ("NAME") ENABLE
   );

CREATE TABLE "ENDPOINT_CONFIG"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	"DESCRIPTION" VARCHAR2(255 CHAR),
	"METHOD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"OUTPUT_ENCODING" VARCHAR2(255 CHAR),
	"PATH" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"IS_SEQUENTIAL" NUMBER(1,0) NOT NULL ENABLE,
	"UPDATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	"IDEMPOTENCY_ENABLED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE,
	"IDEMPOTENCY_TTL_SECONDS" NUMBER(10,0) DEFAULT 86400 NOT NULL ENABLE,
	"PARALLEL_EXECUTION" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE,
	"RESPONSE_CACHE_ENABLED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE,
	"RESPONSE_CACHE_TTL_SECONDS" NUMBER(10,0) DEFAULT 300 NOT NULL ENABLE,
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CHECK (is_sequential in (0,1)) ENABLE,
	 CHECK (idempotency_enabled in (0,1)) ENABLE,
	 CHECK (parallel_execution in (0,1)) ENABLE,
	 CHECK (response_cache_enabled in (0,1)) ENABLE,
	 CONSTRAINT "ENDPOINT_CONFIG_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK5SBR9SP37R2WRGTA6BTBEE3XB" UNIQUE ("PATH") ENABLE
   );

CREATE TABLE "BACKEND_STEP"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"FORWARD_ORIGINAL_BODY" NUMBER(1,0) NOT NULL ENABLE,
	"GROUP_NAME" VARCHAR2(255 CHAR),
	"METHOD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"STEP_ORDER" NUMBER(10,0) NOT NULL ENABLE,
	"TARGET_FIELD" VARCHAR2(255 CHAR),
	"URL_PATTERN" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"ENDPOINT_ID" VARCHAR2(255 CHAR),
	"UPSTREAM_SERVICE_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	-- NUMBER(1,0)+CHECK (khong dung BOOLEAN - kieu do CHI co tu Oracle 23c tro
	-- len, dung lam kieu cot bang tren 19c se loi ORA-00902) - xem giai thich
	-- day du trong ddl-gateway-manager.sql.
	"CACHE_ENABLED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE,
	"CACHE_TTL_SECONDS" NUMBER(10,0) DEFAULT 300 NOT NULL ENABLE,
	"CANVAS_X" NUMBER(10,0),
	"CANVAS_Y" NUMBER(10,0),
	"CONDITION_EXPECTED_VALUE" VARCHAR2(255 CHAR),
	"CONDITION_OPERATOR" VARCHAR2(255 CHAR),
	"CONDITION_SOURCE_FIELD" VARCHAR2(255 CHAR),
	"CONDITION_SOURCE_STEP_ORDER" NUMBER(10,0),
	"CONDITION_SOURCE_TYPE" VARCHAR2(255 CHAR),
	"NEXT_STEP_ORDER_IF_FALSE" NUMBER(10,0),
	"NEXT_STEP_ORDER_IF_TRUE" NUMBER(10,0),
	"CONNECT_TIMEOUT_MS" NUMBER(10,0),
	"READ_TIMEOUT_MS" NUMBER(10,0),
	"ON_ERROR_STEP_ORDER" NUMBER(10,0),
	"PARALLEL_GROUP" NUMBER(10,0),
	"COMPENSATION_UPSTREAM_SERVICE_ID" VARCHAR2(255 CHAR),
	"COMPENSATION_METHOD" VARCHAR2(255 CHAR),
	"COMPENSATION_URL_PATTERN" VARCHAR2(255 CHAR),
	 CHECK (forward_original_body in (0,1)) ENABLE,
	 CHECK (cache_enabled in (0,1)) ENABLE,
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CHECK ((condition_operator in ('EQUALS','NOT_EQUALS','EXISTS','NOT_EXISTS','GREATER_THAN','GREATER_THAN_OR_EQUAL','LESS_THAN','LESS_THAN_OR_EQUAL'))) ENABLE,
	 CHECK ((condition_source_type in ('STEP_RESPONSE','REQUEST_BODY','STEP_RESPONSE_ARRAY_AGGREGATE'))) ENABLE,
	 CHECK (compensation_method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CONSTRAINT "BACKEND_STEP_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FKRTN26VYRBRFLYH5FA838XWTL2" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE,
	 CONSTRAINT "FK2RCXPBA4YOIDGMRN8I0S3OFTT" FOREIGN KEY ("UPSTREAM_SERVICE_ID")
	  REFERENCES "UPSTREAM_SERVICE" ("ID") ENABLE,
	 CONSTRAINT "BACKEND_STEP_COMP_UPSTREAM_FK" FOREIGN KEY ("COMPENSATION_UPSTREAM_SERVICE_ID")
	  REFERENCES "UPSTREAM_SERVICE" ("ID") ENABLE
   );

CREATE TABLE "BACKEND_STEP_ALLOW"
   (	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"FIELD_NAME" VARCHAR2(255 CHAR),
	 CONSTRAINT "FKLJYTFUDPJNU325RXYAG45C0DT" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

CREATE TABLE "BACKEND_STEP_DENY"
   (	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"FIELD_NAME" VARCHAR2(255 CHAR),
	 CONSTRAINT "FKQXQIJEVJM7G8Y9BQOXSQ57NHF" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

CREATE TABLE "BACKEND_STEP_MAPPING"
   (	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"TARGET_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_FIELD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 CONSTRAINT "BACKEND_STEP_MAPPING_PK" PRIMARY KEY ("STEP_ID", "SOURCE_FIELD") ENABLE,
	 CONSTRAINT "FKT06QSCVM4OA358VRUBIG5ACMD" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

CREATE TABLE "FIELD_MAPPING"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"SOURCE_ARRAY_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_ELEMENT_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_STEP_ORDER" NUMBER(10,0),
	"SOURCE_TYPE" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CONSTANT_VALUE" VARCHAR2(4000 CHAR),
	"TARGET_PARAM_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"TARGET_STEP_ORDER" NUMBER(10,0) NOT NULL ENABLE,
	"TARGET_TYPE" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"ENDPOINT_ID" VARCHAR2(255 CHAR),
	"MAPPING_ORDER" NUMBER(10,0) DEFAULT 0 NOT NULL ENABLE,
	"TARGET_CONTEXT" VARCHAR2(255 CHAR) DEFAULT 'MAIN' NOT NULL ENABLE,
	 CONSTRAINT "FIELD_MAPPING_SOURCE_TYPE_CHK" CHECK (source_type in ('STEP_RESPONSE','REQUEST_BODY','QUERY_PARAM','STEP_RESPONSE_ARRAY_AGGREGATE','CONSTANT','STEP_RESPONSE_ARRAY_MERGE')) ENABLE,
	 CHECK (target_type in ('PATH','QUERY','HEADER','BODY_FIELD')) ENABLE,
	 CONSTRAINT "FIELD_MAPPING_TARGET_CONTEXT_CHK" CHECK (target_context in ('MAIN','COMPENSATION')) ENABLE,
	 CONSTRAINT "FIELD_MAPPING_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FKG9BDKEU7BU7USNQSCONIOKV8K" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE
   );

CREATE TABLE "ENDPOINT_CONFIG_VERSION"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CHANGE_TYPE" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (9) WITH TIME ZONE,
	"ENDPOINT_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"METHOD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"PATH" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"SNAPSHOT_JSON" CLOB NOT NULL ENABLE,
	"VERSION_NUMBER" NUMBER(10,0) NOT NULL ENABLE,
	 CHECK ((change_type in ('CREATED','UPDATED','ROLLED_BACK'))) ENABLE,
	 CHECK ((method in ('GET','POST','PUT','DELETE','PATCH'))) ENABLE,
	 CONSTRAINT "ENDPOINT_CONFIG_VERSION_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK2NPOJ2PDGV4HFROGTJSFCPQXU" UNIQUE ("ENDPOINT_ID", "VERSION_NUMBER") ENABLE
   );

CREATE INDEX "IDX_ECV_ENDPOINT_ID" ON "ENDPOINT_CONFIG_VERSION" ("ENDPOINT_ID");

-- ============================================================================
-- [2] INSERT du lieu THAT (4 Upstream + 2 Endpoint composite + 4 Step +
--     12 FieldMapping + 3 phien ban lich su) - da sua khop schema/enum hien tai
-- ============================================================================

-- UPSTREAM_SERVICE (4 row)
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT) VALUES ('53297859-835d-4d06-b4e0-ae3ce51ba943','http://10.207.252.17:8045',1,1000,TIMESTAMP '2026-08-27 11:17:17.791369966 +00:00','BCCS product-catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap (B2)',50,'product-catalog-service',3000,1,TIMESTAMP '2026-08-27 11:17:17.791376994 +00:00');
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT) VALUES ('d07b5e77-d6eb-41c4-b235-274107f164d9','http://10.207.252.17:8884',1,1000,TIMESTAMP '2026-08-27 11:17:17.803327699 +00:00','BCCS stock-management - search-locked-isdn-by-user (B3)',50,'stock-management',5000,1,TIMESTAMP '2026-08-27 11:17:17.803334720 +00:00');
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT) VALUES ('0992a01c-56e6-4de1-8ffb-9e4196ae4eba','http://10.207.252.17:8045',1,1000,TIMESTAMP '2026-08-26 04:20:22.888103600 +00:00','Product catalog service (remote 10.207.252.17:8045) - cache ON',50,'catalog-service',5000,1,TIMESTAMP '2026-08-27 08:08:39.413598141 +00:00');
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT) VALUES ('0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf','http://10.207.252.17:8884',1,1000,TIMESTAMP '2026-08-26 04:20:23.192384200 +00:00','Stock management search-isdn-by-stock-model (remote 10.207.252.17:8884)',50,'stock-management-service',5000,1,TIMESTAMP '2026-08-27 08:08:32.148183098 +00:00');

-- ENDPOINT_CONFIG (2 row)
INSERT INTO ENDPOINT_CONFIG (ID,IS_SEQUENTIAL,CREATED_AT,UPDATED_AT,DESCRIPTION,METHOD,NAME,OUTPUT_ENCODING,PATH) VALUES ('0ad45d66-9dbe-42c0-b460-b0fa57874ff2',1,TIMESTAMP '2026-08-27 11:17:17.851596997 +00:00',TIMESTAMP '2026-08-27 11:17:17.851604421 +00:00','Yeucau yeucau: B1 GET /vcom/search-locked-isdn-by-user?staffCode=... -> B2 product-catalog (fixed params) -> B3 stock-management (body = data B2) -> tra ket qua','GET','Search locked ISDN by user (composite)','json','/vcom/search-locked-isdn-by-user');
INSERT INTO ENDPOINT_CONFIG (ID,IS_SEQUENTIAL,CREATED_AT,UPDATED_AT,DESCRIPTION,METHOD,NAME,OUTPUT_ENCODING,PATH) VALUES ('3b79585a-0d94-45b2-82d2-f1cfcfa97e96',1,TIMESTAMP '2026-08-26 04:21:19.420594200 +00:00',TIMESTAMP '2026-08-26 04:21:19.420594200 +00:00','Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst','POST','Tim ISDN theo Stock Model (composite)','json','/v1/isdn-search-by-stock-model');

-- BACKEND_STEP (4 row)
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('7fe7d102-80b4-41de-bb0b-8d293040b1ff',0,1,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,'GET','Product Catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap',NULL,'53297859-835d-4d06-b4e0-ae3ce51ba943','/product-catalog-service/v1/product/findByTelecomSubTypeOfferTypeCheckProductStatusMap?telecomServiceId=1&offerTypeId=1&getActiveProduct=true',0,300,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('2cf0c588-f8f3-48d4-bdbd-8c9e9ba1cfa1',0,2,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,'POST','Stock Management - search-locked-isdn-by-user',NULL,'d07b5e77-d6eb-41c4-b235-274107f164d9','/stock-management/stock-number/search-locked-isdn-by-user',0,300,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('b90b1f10-b349-440e-bc8f-e150e29071e4',0,1,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,'GET','Catalog - findByTelecomSubTypeOfferType',NULL,'0992a01c-56e6-4de1-8ffb-9e4196ae4eba','/product-catalog-service/v1/product/findByTelecomSubTypeOfferType',0,300,479,166,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('1507f489-4566-4e2d-a5b1-4d985c4700e7',0,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,'POST','Stock - search-isdn-by-stock-model',NULL,'0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf','/stock-management/stock-number/search-isdn-by-stock-model',0,300,483,11,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

-- BACKEND_STEP_ALLOW / DENY / MAPPING: khong co du lieu

-- FIELD_MAPPING (12 row) - dong dau tien: SOURCE_TYPE da doi tu 'REQUEST_QUERY' (dump goc) sang 'QUERY_PARAM' (dung enum hien tai)
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('d8615ed8-f222-4b0f-a803-dad6d8183bab',NULL,2,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,NULL,'staffCode','QUERY_PARAM','staffCode','QUERY',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('9a8a95fa-8851-469e-95f9-d26331cd58b7',1,2,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,NULL,'data','STEP_RESPONSE','$body','BODY_FIELD',1);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('910528f4-d7f9-4f04-bb35-ac38c185ebcc',NULL,1,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'telecomServiceId','REQUEST_BODY','telecomServiceId','QUERY',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('24b30320-fcd4-4e77-ac8c-a8e265ec3129',NULL,1,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'subType','REQUEST_BODY','subType','QUERY',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('0e165292-3fde-409e-b67b-29712d747253',NULL,1,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'offerTypeId','REQUEST_BODY','offerTypeId','QUERY',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('6c0cde5a-ccaf-471f-8209-fc6ad3f97be0',1,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96','data','productOfferingId',NULL,'STEP_RESPONSE_ARRAY_AGGREGATE','stockModelIds','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('62a6e27c-e2b7-4021-b2f8-8d1544a895de',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'staffCode','REQUEST_BODY','staffCode','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('8343453a-540e-48c5-8556-9c25cdd207f4',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'typeIsdn','REQUEST_BODY','typeIsdn','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('c19cdf93-3cfb-43e4-becf-6a4e93e4164e',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'shopIds','REQUEST_BODY','shopIds','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('dc4b7682-3b13-458b-ab82-3cce2226b942',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'telecomServiceId','REQUEST_BODY','telecomServiceId','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('d3f0829f-3575-41c8-900e-564d4dc8d252',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'page','REQUEST_BODY','page','BODY_FIELD',0);
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('efdd274a-d370-46f8-a6b4-6f95c2ef0ba7',NULL,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,NULL,'pageSize','REQUEST_BODY','pageSize','BODY_FIELD',0);

-- ENDPOINT_CONFIG_VERSION (3 row) - SNAPSHOT_JSON (CLOB) chia thanh nhieu TO_CLOB(...) || ... (<2499 ky tu/dong cho sqlplus)
-- Version #1 cua endpoint /vcom/search-locked-isdn-by-user: sourceType trong JSON da doi 'REQUEST_QUERY' -> 'QUERY_PARAM' (tranh loi deserialize khi bam "rollback" ve version nay sau nay)
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('1a6ce6e8-cc54-409c-a94f-fb0c04a0d0da','CREATED',TIMESTAMP '2026-08-27 11:17:17.949306582 +00:00','0ad45d66-9dbe-42c0-b460-b0fa57874ff2','GET','Search locked ISDN by user (composite)','/vcom/search-locked-isdn-by-user',TO_CLOB('{"id":"0ad45d66-9dbe-42c0-b460-b0fa57874ff2","name":"Search locked ISDN by user (composite)","description":"Yeucau yeucau: B1 GET /vcom/search-locked-isdn-by-user?staffCode=... -> B2 product-catalog (fixed params) -> B3 stock-management (body = data B2) -> tra ket qua","path":"/vcom/search-locked-isdn-by-user","method":"GET","sequential":true,"outputEncoding":"json","steps":[{"id":"7fe7d102-80b4-41de-bb0b-8d293040b1ff","stepOrder":1,"name":"Product Catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferTypeCheckProductStatusMap?telecomServiceId=1&offerTypeId=1&getActiveProduct=true","upstreamServiceId":"53297859-835d-4d06-b4e0-ae3ce51ba943","upstreamServiceName":"product-catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{}},{"id":"2cf0c588-f8f3-48d4-bdbd-8c9e9ba1cfa1","stepOrder":2,"name":"Stock Mana')
     || TO_CLOB('gement - search-locked-isdn-by-user","method":"POST","urlPattern":"/stock-management/stock-number/search-locked-isdn-by-user","upstreamServiceId":"d07b5e77-d6eb-41c4-b235-274107f164d9","upstreamServiceName":"stock-management","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{}}],"mappings":[{"id":"d8615ed8-f222-4b0f-a803-dad6d8183bab","sourceType":"QUERY_PARAM","sourceField":"staffCode","targetStepOrder":2,"targetType":"QUERY","targetParamName":"staffCode","mappingOrder":0},{"id":"9a8a95fa-8851-469e-95f9-d26331cd58b7","sourceType":"STEP_RESPONSE","sourceStepOrder":1,"sourceField":"data","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"$body","mappingOrder":1}],"createdAt":"2026-08-27T11:17:17.851596997Z","updatedAt":"2026-08-27T11:17:17.851604421Z"}'),1);
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('3e3b51e0-0ff5-418f-b306-a9a9cb22cfc7','UPDATED',TIMESTAMP '2026-08-27 06:35:16.464117230 +00:00','3b79585a-0d94-45b2-82d2-f1cfcfa97e96','POST','Tim ISDN theo Stock Model (composite)','/v1/isdn-search-by-stock-model',TO_CLOB('{"id":"3b79585a-0d94-45b2-82d2-f1cfcfa97e96","name":"Tim ISDN theo Stock Model (composite)","description":"Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst","path":"/v1/isdn-search-by-stock-model","method":"POST","sequential":true,"outputEncoding":"json","steps":[{"id":"721073de-4e5b-4cce-a846-7b3a5d0d9099","stepOrder":1,"name":"Catalog - findByTelecomSubTypeOfferType","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferType","upstreamServiceId":"0992a01c-56e6-4de1-8ffb-9e4196ae4eba","upstreamServiceName":"catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":548,"canvasY":244},{"id":"9b256848-c647-4934-a84c-1e3a447c71ba","stepOrder":2,"name":"Stock - search-isdn-by-stock-model","method":"POST","urlPattern":"/stock-management/stock-number/sear')
     || TO_CLOB('ch-isdn-by-stock-model","upstreamServiceId":"0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf","upstreamServiceName":"stock-management-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":893,"canvasY":36}],"mappings":[{"id":"012ff902-6a27-4717-8cf0-480ff646f5a0","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"9d992fde-1d0f-442a-a458-8df7378f29fa","sourceType":"REQUEST_BODY","sourceField":"subType","targetStepOrder":1,"targetType":"QUERY","targetParamName":"subType","mappingOrder":0},{"id":"132e6db8-76e5-4f30-b84e-c43f66e3f98d","sourceType":"REQUEST_BODY","sourceField":"offerTypeId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"offerTypeId","mappingOrder":0},{"id":"0aedef7b-858c-4d64-82f9-617654247d46","sourceType":"STEP_RESPONSE_ARRAY_AGGREGATE","sourceStepOrder":1,"sourceArra')
     || TO_CLOB('yField":"data","sourceElementField":"productOfferingId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"stockModelIds","mappingOrder":0},{"id":"4bc4e9cd-5fdf-4150-bb2a-5a0588494080","sourceType":"REQUEST_BODY","sourceField":"pageSize","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"pageSize","mappingOrder":0},{"id":"4eb2133d-e6a7-4688-8a5b-3fc6aa22e50f","sourceType":"REQUEST_BODY","sourceField":"typeIsdn","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"typeIsdn","mappingOrder":0},{"id":"f57ef7ca-28dd-4a78-ba1c-390b60856586","sourceType":"REQUEST_BODY","sourceField":"shopIds","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"shopIds","mappingOrder":0},{"id":"ed2f4548-09de-41ee-a9e1-7b1c16946faa","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"fe06aa0b-a4bc-494b-9749-8c31b9a19ca5","sourceType":"REQUEST_B')
     || TO_CLOB('ODY","sourceField":"page","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"page","mappingOrder":0},{"id":"56d114dd-d3d8-40f8-a0da-a0483bb19cb3","sourceType":"REQUEST_BODY","sourceField":"staffCode","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"staffCode","mappingOrder":0}],"createdAt":"2026-08-26T04:21:19.420594200Z","updatedAt":"2026-08-26T04:21:19.420594200Z"}'),1);
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('4256ad24-42b7-4234-a22e-f5dc2fe12a96','UPDATED',TIMESTAMP '2026-08-27 06:35:28.646321861 +00:00','3b79585a-0d94-45b2-82d2-f1cfcfa97e96','POST','Tim ISDN theo Stock Model (composite)','/v1/isdn-search-by-stock-model',TO_CLOB('{"id":"3b79585a-0d94-45b2-82d2-f1cfcfa97e96","name":"Tim ISDN theo Stock Model (composite)","description":"Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst","path":"/v1/isdn-search-by-stock-model","method":"POST","sequential":true,"outputEncoding":"json","steps":[{"id":"b90b1f10-b349-440e-bc8f-e150e29071e4","stepOrder":1,"name":"Catalog - findByTelecomSubTypeOfferType","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferType","upstreamServiceId":"0992a01c-56e6-4de1-8ffb-9e4196ae4eba","upstreamServiceName":"catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":479,"canvasY":166},{"id":"1507f489-4566-4e2d-a5b1-4d985c4700e7","stepOrder":2,"name":"Stock - search-isdn-by-stock-model","method":"POST","urlPattern":"/stock-management/stock-number/sear')
     || TO_CLOB('ch-isdn-by-stock-model","upstreamServiceId":"0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf","upstreamServiceName":"stock-management-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":483,"canvasY":11}],"mappings":[{"id":"910528f4-d7f9-4f04-bb35-ac38c185ebcc","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"24b30320-fcd4-4e77-ac8c-a8e265ec3129","sourceType":"REQUEST_BODY","sourceField":"subType","targetStepOrder":1,"targetType":"QUERY","targetParamName":"subType","mappingOrder":0},{"id":"0e165292-3fde-409e-b67b-29712d747253","sourceType":"REQUEST_BODY","sourceField":"offerTypeId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"offerTypeId","mappingOrder":0},{"id":"6c0cde5a-ccaf-471f-8209-fc6ad3f97be0","sourceType":"STEP_RESPONSE_ARRAY_AGGREGATE","sourceStepOrder":1,"sourceArra')
     || TO_CLOB('yField":"data","sourceElementField":"productOfferingId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"stockModelIds","mappingOrder":0},{"id":"62a6e27c-e2b7-4021-b2f8-8d1544a895de","sourceType":"REQUEST_BODY","sourceField":"staffCode","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"staffCode","mappingOrder":0},{"id":"8343453a-540e-48c5-8556-9c25cdd207f4","sourceType":"REQUEST_BODY","sourceField":"typeIsdn","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"typeIsdn","mappingOrder":0},{"id":"c19cdf93-3cfb-43e4-becf-6a4e93e4164e","sourceType":"REQUEST_BODY","sourceField":"shopIds","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"shopIds","mappingOrder":0},{"id":"dc4b7682-3b13-458b-ab82-3cce2226b942","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"d3f0829f-3575-41c8-900e-564d4dc8d252","sourceType":"REQUEST')
     || TO_CLOB('_BODY","sourceField":"page","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"page","mappingOrder":0},{"id":"efdd274a-d370-46f8-a6b4-6f95c2ef0ba7","sourceType":"REQUEST_BODY","sourceField":"pageSize","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"pageSize","mappingOrder":0}],"createdAt":"2026-08-26T04:21:19.420594200Z","updatedAt":"2026-08-26T04:21:19.420594200Z"}'),2);

COMMIT;

-- ============================================================================
-- HET FILE gateway-manager-production-setup.sql
-- ============================================================================
