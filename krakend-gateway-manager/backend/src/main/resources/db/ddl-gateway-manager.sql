-- ============================================================================
-- DDL cua 8 bang thuoc krakend-gateway-manager, trich xuat THAT tu Oracle dang
-- chay (schema BCCS_PRODUCT, qua DBMS_METADATA.GET_DDL - khong phai suy doan
-- tu @Entity) ngay 2026-08-27. Dung de dung schema tren mot Oracle khac khong
-- muon/khong the de Hibernate tu ALTER (bccs.jpa.hibernate.ddl-auto=update
-- trong application.yml se TU sinh dung cac bang nay neu chay thang app vao
-- schema rong - script nay chi la ban chup mang lai, KHONG bat buoc phai chay
-- tay neu dung duoc ddl-auto).
--
-- Schema BCCS_PRODUCT dang dung CHUNG voi cac service BCCS khac (organization-
-- resource-service, product-catalog-service...) - script nay CHI gom 8 bang
-- rieng cua krakend-gateway-manager, khong dung cham cac bang cua service khac
-- (SHOP, STAFF, PRODUCT_OFFERING...) dang nam chung 1 schema.
--
-- Cap nhat 2026-08-27 (lan 1): them 'QUERY_PARAM' vao CHECK constraint cua
-- FIELD_MAPPING.SOURCE_TYPE (doi ten sang FIELD_MAPPING_SOURCE_TYPE_CHK, khong
-- con la ten SYS_C#### tu dong nua) - xem README.md muc 8 "Gioi han hien tai"
-- ve viec ddl-auto=update KHONG tu noi CHECK constraint cho enum da co san.
-- Cap nhat 2026-08-27 (lan 2): them CONNECT_TIMEOUT_MS/READ_TIMEOUT_MS vao
-- BACKEND_STEP (tinh nang override timeout theo tung step) - lan trich xuat
-- truoc do (lan 1) da bo sot 2 cot nay.
-- Cap nhat 2026-08-28 (lan 8): them cot IDEMPOTENCY_ENABLED (NUMBER(1,0), mac dinh 0) +
-- IDEMPOTENCY_TTL_SECONDS (NUMBER(10,0), mac dinh 86400) vao ENDPOINT_CONFIG - bat
-- idempotency-key theo tung endpoint (client gui header "Idempotency-Key", cache
-- response THANH CONG qua GatewayCacheService, tranh goi lai backend that khi client tu
-- dong retry). Tren DB DA CO bang nay, chay tay:
-- ALTER TABLE endpoint_config ADD idempotency_enabled NUMBER(1,0) DEFAULT 0 NOT NULL;
-- ALTER TABLE endpoint_config ADD CONSTRAINT endpoint_config_idempotency_chk
--   CHECK (idempotency_enabled in (0,1));
-- ALTER TABLE endpoint_config ADD idempotency_ttl_seconds NUMBER(10,0) DEFAULT 86400 NOT NULL;
-- Cap nhat 2026-08-28 (lan 7): them 'STEP_RESPONSE_ARRAY_MERGE' vao CHECK constraint
-- cua FIELD_MAPPING.SOURCE_TYPE - gop TOAN BO field cua tung phan tu (object) trong 1
-- mang thanh 1 OBJECT DUY NHAT (khac STEP_RESPONSE_ARRAY_AGGREGATE - trich 1 field ten
-- co dinh tu moi phan tu thanh 1 MANG). Khong them cot moi (tai dung sourceArrayField
-- da co). Tren DB DA CO bang nay, chay tay:
-- ALTER TABLE field_mapping DROP CONSTRAINT FIELD_MAPPING_SOURCE_TYPE_CHK;
-- ALTER TABLE field_mapping ADD CONSTRAINT FIELD_MAPPING_SOURCE_TYPE_CHK
--   CHECK (source_type in ('STEP_RESPONSE','REQUEST_BODY','QUERY_PARAM',
--   'STEP_RESPONSE_ARRAY_AGGREGATE','CONSTANT','STEP_RESPONSE_ARRAY_MERGE'));
-- Cap nhat 2026-08-28 (lan 6): FIELD_MAPPING.CONSTANT_VALUE luc dau khong ghi ro
-- length tren @Column cua entity (JPA mac dinh 255) - ALTER TABLE tay len 4000 (lan
-- 5 duoi day) bi Hibernate ddl-auto=update TU Y NOI LAI VE 255 o lan restart backend
-- ke tiep (khop theo dung entity). Da sua: ghi ro length=4000 tren entity + ALTER lai
-- 4000 tren DB dev - can chay lai tay neu DB da tung bi noi ve 255:
-- ALTER TABLE field_mapping MODIFY constant_value VARCHAR2(4000 CHAR);
-- Cap nhat 2026-08-28 (lan 5): them 'CONSTANT' vao CHECK constraint cua
-- FIELD_MAPPING.SOURCE_TYPE + cot moi FIELD_MAPPING.CONSTANT_VALUE - dung khai
-- bao gia tri hang so co dinh (khong doc tu request/response nao) cho
-- BODY_FIELD/PATH/QUERY/HEADER, vi du 2 nhanh re cung goi 1 API nhung fix cung
-- khac nhau 1 field trong body. Tren DB DA CO bang nay, chay tay:
-- ALTER TABLE field_mapping ADD constant_value VARCHAR2(4000 CHAR);
-- ALTER TABLE field_mapping DROP CONSTRAINT FIELD_MAPPING_SOURCE_TYPE_CHK;
-- ALTER TABLE field_mapping ADD CONSTRAINT FIELD_MAPPING_SOURCE_TYPE_CHK
--   CHECK (source_type in ('STEP_RESPONSE','REQUEST_BODY','QUERY_PARAM',
--   'STEP_RESPONSE_ARRAY_AGGREGATE','CONSTANT'));
-- Cap nhat 2026-08-27 (lan 4): them 4 toan tu so sanh so (GREATER_THAN,
-- GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL) vao CHECK constraint
-- cua BACKEND_STEP.CONDITION_OPERATOR - dung cho re nhanh so (vd "response
-- step truoc <=3 thi goi nhanh A, >3 thi goi nhanh B"). Tren DB DA CO bang
-- nay, chay tay: ALTER TABLE backend_step DROP CONSTRAINT <ten hien tai
-- - vd SYS_C#### hoac backend_step_condition_op_chk>; ALTER TABLE
-- backend_step ADD CONSTRAINT backend_step_condition_op_chk CHECK
-- (condition_operator in ('EQUALS','NOT_EQUALS','EXISTS','NOT_EXISTS',
-- 'GREATER_THAN','GREATER_THAN_OR_EQUAL','LESS_THAN','LESS_THAN_OR_EQUAL'));
--
-- Thu tu tao bang theo dung phu thuoc FK (bang cha truoc, bang con sau).
-- Ten constraint/index giu NGUYEN theo dung Hibernate da tu sinh tren may hien
-- tai (vd FKRTN26VYRBRFLYH5FA838XWTL2) - neu chay lai script nay tren may khac
-- roi de app tu ket noi voi ddl-auto=update, Hibernate se tu nhan dien schema
-- da khop, khong ALTER gi them.
--
-- Cap nhat 2026-08-27 (lan 3): them buoc [0] DROP-neu-da-ton-tai (idempotent)
-- truoc moi CREATE TABLE - chay lai script tren schema DA CO 8 bang nay se
-- khong con loi ORA-00955. !!! CANH BAO: buoc [0] XOA HAN (khong hoan tac
-- duoc) ca 8 bang neu da ton tai, ke ca du lieu that dang co trong do - chi
-- chay file nay neu chac chan muon thay the toan bo, da backup truoc neu can
-- giu du lieu cu. !!!
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. DROP neu da ton tai (idempotent) - thu tu NGUOC voi luc tao, CASCADE
--    CONSTRAINTS de tu go FK tham chieu, PURGE de giai phong ten bang ngay.
--    Bo qua an toan (khong bao loi) neu bang chua ton tai (ORA-00942).
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- 1. UPSTREAM_SERVICE - dang ky 1 backend that (host/timeout/resilience)
-- ----------------------------------------------------------------------------
CREATE TABLE "UPSTREAM_SERVICE"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"BASE_HOST" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CIRCUIT_BREAKER_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"CONNECT_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	"DESCRIPTION" VARCHAR2(255 CHAR),
	"FAILURE_RATE_THRESHOLD" NUMBER(10,0) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"READ_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"RETRY_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"UPDATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	 CHECK (circuit_breaker_enabled in (0,1)) ENABLE,
	 CHECK (retry_enabled in (0,1)) ENABLE,
	 CONSTRAINT "UPSTREAM_SERVICE_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UKR9K2UNQ519ISGPHWC5EMCHYUC" UNIQUE ("NAME") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 2. ENDPOINT_CONFIG - 1 endpoint client-facing (path/method + co composite hay khong)
-- ----------------------------------------------------------------------------
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
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CHECK (is_sequential in (0,1)) ENABLE,
	 CHECK (idempotency_enabled in (0,1)) ENABLE,
	 CONSTRAINT "ENDPOINT_CONFIG_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK5SBR9SP37R2WRGTA6BTBEE3XB" UNIQUE ("PATH") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 3. BACKEND_STEP - 1 lan goi ra UpstreamService trong chuoi cua 1 Endpoint
-- ----------------------------------------------------------------------------
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
	-- Tren Oracle dev that (23ai) cot nay duoc Hibernate tu sinh kieu BOOLEAN
	-- native (chi co tu Oracle 23c tro len - xem @ColumnDefault trong
	-- BackendStep.java) vi ALTER TABLE them cot nay vao SAU, dung luc dialect
	-- da chuyen sang BOOLEAN. Script nay CHU DICH doi lai NUMBER(1,0)+CHECK
	-- (giong het FORWARD_ORIGINAL_BODY o tren) de tuong thich Oracle 19c -
	-- BOOLEAN lam kieu COT BANG se loi ORA-00902 tren 19c (BOOLEAN truoc 23c
	-- chi dung duoc trong PL/SQL, khong dung lam kieu cot bang).
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
	-- Override connectTimeout/readTimeout rieng cho step nay (nullable, khong
	-- @ColumnDefault - xem BackendStep.java) - null = dung mac dinh UpstreamService.
	"CONNECT_TIMEOUT_MS" NUMBER(10,0),
	"READ_TIMEOUT_MS" NUMBER(10,0),
	 CHECK (forward_original_body in (0,1)) ENABLE,
	 CHECK (cache_enabled in (0,1)) ENABLE,
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CHECK ((condition_operator in ('EQUALS','NOT_EQUALS','EXISTS','NOT_EXISTS','GREATER_THAN','GREATER_THAN_OR_EQUAL','LESS_THAN','LESS_THAN_OR_EQUAL'))) ENABLE,
	 CHECK ((condition_source_type in ('STEP_RESPONSE','REQUEST_BODY','STEP_RESPONSE_ARRAY_AGGREGATE'))) ENABLE,
	 CONSTRAINT "BACKEND_STEP_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FKRTN26VYRBRFLYH5FA838XWTL2" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE,
	 CONSTRAINT "FK2RCXPBA4YOIDGMRN8I0S3OFTT" FOREIGN KEY ("UPSTREAM_SERVICE_ID")
	  REFERENCES "UPSTREAM_SERVICE" ("ID") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 4-6. Bang phu (@ElementCollection) cua BACKEND_STEP - allow/deny/rename field
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- 7. FIELD_MAPPING - "lay 1 gia tri tu dau, bom vao dau" giua cac step
-- ----------------------------------------------------------------------------
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
	 CONSTRAINT "FIELD_MAPPING_SOURCE_TYPE_CHK" CHECK (source_type in ('STEP_RESPONSE','REQUEST_BODY','QUERY_PARAM','STEP_RESPONSE_ARRAY_AGGREGATE','CONSTANT','STEP_RESPONSE_ARRAY_MERGE')) ENABLE,
	 CHECK (target_type in ('PATH','QUERY','HEADER','BODY_FIELD')) ENABLE,
	 CONSTRAINT "FIELD_MAPPING_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FKG9BDKEU7BU7USNQSCONIOKV8K" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 8. ENDPOINT_CONFIG_VERSION - snapshot lich su phien ban (khong dung FK toi
--    ENDPOINT_CONFIG - co tinh giu endpoint_id dang String phang, xem comment
--    trong entity/EndpointConfigVersion.java)
-- ----------------------------------------------------------------------------
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
