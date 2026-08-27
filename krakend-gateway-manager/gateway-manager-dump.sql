-- ============================================================================
-- Gateway Manager - Full dump (DDL + data) cua 8 bang config
-- Nguon: Oracle SAN XUAT/test  DB170 (10.207.222.170:1521), schema BCCS_PRODUCT
-- Xuat ngay: 2026-08-27
-- ----------------------------------------------------------------------------
-- NOI DUNG:
--   [1] DDL (CREATE TABLE + constraint) cho 8 bang config cua krakend-gateway-manager
--   [2] INSERT du lieu DAU (toan bo so lieu hien tai o DB170: 4 upstream,
--       2 endpoint, 4 backend_step, 12 field_mapping, 3 endpoint_config_version;
--       cac bang phu BACKEND_STEP_ALLOW/DENY/MAPPING dang rong -> khong co INSERT)
--
-- HUONG DAN DUNG:
--   * Chay len 1 Oracle (O19c tro len), dung schema nao thi khai bao trong admin
--     + dat lenh DDL/INSERT khong co prefix schema (dung schema hien tai).
--   * DDL thuong dung cho target TRONG (chua co 8 bang nay). Neu bang da ton tai,
--     DROP truoc hoac bo qua. INSERT dung sau DDL.
--   * Dat lai thu tu: UPSTREAM_SERVICE -> ENDPOINT_CONFIG -> BACKEND_STEP ->
--     (BACKEND_STEP_ALLOW / BACKEND_STEP_DENY / BACKEND_STEP_MAPPING) ->
--     FIELD_MAPPING -> ENDPOINT_CONFIG_VERSION (dung thu tu FK).
--   * Cac bang nam CHUNG schema BCCS_PRODUCT voi cac service BCCS khac (SHOP,
--     STAFF, PRODUCT_OFFERING...) - script nay CHI dung 8 bang rieng cua
--     krakend-gateway-manager, khong dung cham bang nghiep vu khac.
-- ============================================================================
-- Cau hinh phien lam viec (sqlplus)
--   * SET DEFINE OFF: tat ky tu thay the bien (&) - bat buoc vi URL_PATTERN cua
--     BACKEND_STEP co chua "&" (?telecomServiceId=1&offerTypeId=1&...) - neu
--     khong tat, sqlplus se hoi "Enter value for offertypeid: ..." va lam hong
--     cau INSERT. Neu chay script nay tren SQLcl (Java) thi khong can, nhung
--     de an toan cho ca hai thi giu nguyen.
-- ============================================================================
SET DEFINE OFF

-- ============================================================================
-- [1] DDL - 8 bang config
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. UPSTREAM_SERVICE - dang ky 1 backend that (host/timeout/resilience/cache)
-- ----------------------------------------------------------------------------
CREATE TABLE "UPSTREAM_SERVICE"
   (	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"BASE_HOST" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CIRCUIT_BREAKER_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"CONNECT_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (9) WITH TIME ZONE,
	"DESCRIPTION" VARCHAR2(255 CHAR),
	"FAILURE_RATE_THRESHOLD" NUMBER(10,0) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"READ_TIMEOUT_MS" NUMBER(10,0) NOT NULL ENABLE,
	"RETRY_ENABLED" NUMBER(1,0) NOT NULL ENABLE,
	"UPDATED_AT" TIMESTAMP (9) WITH TIME ZONE,
	"CACHE_ENABLED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE,
	"CACHE_TTL_SECONDS" NUMBER(10,0) DEFAULT 0 NOT NULL ENABLE,
	 CHECK (cache_enabled in (0,1)) ENABLE,
	 CHECK (circuit_breaker_enabled in (0,1)) ENABLE,
	 CHECK (retry_enabled in (0,1)) ENABLE,
	 CONSTRAINT "UPSTREAM_SERVICE_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK_UPSTREAM_NAME" UNIQUE ("NAME") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 2. ENDPOINT_CONFIG - 1 endpoint client-facing (path/method + co composite hay khong)
-- ----------------------------------------------------------------------------
CREATE TABLE "ENDPOINT_CONFIG"
   (	"IS_SEQUENTIAL" NUMBER(1,0) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (9) WITH TIME ZONE,
	"UPDATED_AT" TIMESTAMP (9) WITH TIME ZONE,
	"DESCRIPTION" VARCHAR2(255 CHAR),
	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"METHOD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"OUTPUT_ENCODING" VARCHAR2(255 CHAR),
	"PATH" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 CHECK (is_sequential in (0,1)) ENABLE,
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CONSTRAINT "ENDPOINT_CONFIG_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK_ENDPOINT_PATH" UNIQUE ("PATH") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 3. BACKEND_STEP - 1 lan goi ra UpstreamService trong chuoi cua 1 Endpoint
-- ----------------------------------------------------------------------------
CREATE TABLE "BACKEND_STEP"
   (	"FORWARD_ORIGINAL_BODY" NUMBER(1,0) NOT NULL ENABLE,
	"STEP_ORDER" NUMBER(10,0) NOT NULL ENABLE,
	"ENDPOINT_ID" VARCHAR2(255 CHAR),
	"GROUP_NAME" VARCHAR2(255 CHAR),
	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"METHOD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"TARGET_FIELD" VARCHAR2(255 CHAR),
	"UPSTREAM_SERVICE_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"URL_PATTERN" VARCHAR2(255 CHAR) NOT NULL ENABLE,
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
	 CHECK (forward_original_body in (0,1)) ENABLE,
	 CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CONSTRAINT "CK_BS_COND_OP" CHECK (condition_operator in ('EQUALS','NOT_EQUALS','EXISTS','NOT_EXISTS')) ENABLE,
	 CONSTRAINT "CK_BS_COND_SRC" CHECK (condition_source_type in ('STEP_RESPONSE','REQUEST_BODY','STEP_RESPONSE_ARRAY_AGGREGATE')) ENABLE,
	 CONSTRAINT "BACKEND_STEP_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FK_BS_ENDPOINT" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE,
	 CONSTRAINT "FK_BS_UPSTREAM" FOREIGN KEY ("UPSTREAM_SERVICE_ID")
	  REFERENCES "UPSTREAM_SERVICE" ("ID") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 4-6. Bang phu (@ElementCollection) cua BACKEND_STEP - allow/deny/rename field
-- ----------------------------------------------------------------------------
CREATE TABLE "BACKEND_STEP_ALLOW"
   (	"FIELD_NAME" VARCHAR2(255 CHAR),
	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 CONSTRAINT "FK_ALLOW_STEP" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

CREATE TABLE "BACKEND_STEP_DENY"
   (	"FIELD_NAME" VARCHAR2(255 CHAR),
	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 CONSTRAINT "FK_DENY_STEP" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

CREATE TABLE "BACKEND_STEP_MAPPING"
   (	"SOURCE_FIELD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"STEP_ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"TARGET_FIELD" VARCHAR2(255 CHAR),
	 CONSTRAINT "BACKEND_STEP_MAPPING_PK" PRIMARY KEY ("SOURCE_FIELD", "STEP_ID") ENABLE,
	 CONSTRAINT "FK_MAPPING_STEP" FOREIGN KEY ("STEP_ID")
	  REFERENCES "BACKEND_STEP" ("ID") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 7. FIELD_MAPPING - "lay 1 gia tri tu dau, bom vao dau" giua cac step
-- ----------------------------------------------------------------------------
CREATE TABLE "FIELD_MAPPING"
   (	"SOURCE_STEP_ORDER" NUMBER(10,0),
	"TARGET_STEP_ORDER" NUMBER(10,0) NOT NULL ENABLE,
	"ENDPOINT_ID" VARCHAR2(255 CHAR),
	"ID" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"SOURCE_ARRAY_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_ELEMENT_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_FIELD" VARCHAR2(255 CHAR),
	"SOURCE_TYPE" VARCHAR2(255 CHAR),
	"TARGET_PARAM_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"TARGET_TYPE" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"MAPPING_ORDER" NUMBER(10,0) DEFAULT 0 NOT NULL ENABLE,
	 CHECK (target_type in ('PATH','QUERY','HEADER','BODY_FIELD')) ENABLE,
	 CONSTRAINT "CK_FM_SOURCE_TYPE" CHECK (source_type in ('STEP_RESPONSE','REQUEST_BODY','REQUEST_QUERY','STEP_RESPONSE_ARRAY_AGGREGATE')) ENABLE,
	 CONSTRAINT "FIELD_MAPPING_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "FK_FM_ENDPOINT" FOREIGN KEY ("ENDPOINT_ID")
	  REFERENCES "ENDPOINT_CONFIG" ("ID") ENABLE
   );

-- ----------------------------------------------------------------------------
-- 8. ENDPOINT_CONFIG_VERSION - snapshot lich su phien ban
--    (khong co FK toi ENDPOINT_CONFIG - co tinh giu endpoint_id phang, xem
--    entity/EndpointConfigVersion.java)
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
	 CONSTRAINT "CK_ECV_CHANGE_TYPE" CHECK (change_type in ('CREATED','UPDATED','ROLLED_BACK')) ENABLE,
	 CONSTRAINT "CK_ECV_METHOD" CHECK (method in ('GET','POST','PUT','DELETE','PATCH')) ENABLE,
	 CONSTRAINT "ENDPOINT_CONFIG_VERSION_PK" PRIMARY KEY ("ID") ENABLE,
	 CONSTRAINT "UK_ECV_ENDPOINT_VERSION" UNIQUE ("ENDPOINT_ID", "VERSION_NUMBER") ENABLE
   );

CREATE INDEX "IDX_ECV_ENDPOINT_ID" ON "ENDPOINT_CONFIG_VERSION" ("ENDPOINT_ID");

-- ============================================================================
-- [2] INSERT du lieu (toan bo du lieu hien tai cua DB170)
-- ============================================================================

-- UPSTREAM_SERVICE (4 row)
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT,CACHE_ENABLED,CACHE_TTL_SECONDS) VALUES ('53297859-835d-4d06-b4e0-ae3ce51ba943','http://10.207.252.17:8045',1,1000,TIMESTAMP '2026-08-27 11:17:17.791369966 +00:00','BCCS product-catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap (B2)',50,'product-catalog-service',3000,1,TIMESTAMP '2026-08-27 11:17:17.791376994 +00:00',0,0);
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT,CACHE_ENABLED,CACHE_TTL_SECONDS) VALUES ('d07b5e77-d6eb-41c4-b235-274107f164d9','http://10.207.252.17:8884',1,1000,TIMESTAMP '2026-08-27 11:17:17.803327699 +00:00','BCCS stock-management - search-locked-isdn-by-user (B3)',50,'stock-management',5000,1,TIMESTAMP '2026-08-27 11:17:17.803334720 +00:00',0,0);
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT,CACHE_ENABLED,CACHE_TTL_SECONDS) VALUES ('0992a01c-56e6-4de1-8ffb-9e4196ae4eba','http://10.207.252.17:8045',1,1000,TIMESTAMP '2026-08-26 04:20:22.888103600 +00:00','Product catalog service (remote 10.207.252.17:8045) - cache ON',50,'catalog-service',5000,1,TIMESTAMP '2026-08-27 08:08:39.413598141 +00:00',1,300);
INSERT INTO UPSTREAM_SERVICE (ID,BASE_HOST,CIRCUIT_BREAKER_ENABLED,CONNECT_TIMEOUT_MS,CREATED_AT,DESCRIPTION,FAILURE_RATE_THRESHOLD,NAME,READ_TIMEOUT_MS,RETRY_ENABLED,UPDATED_AT,CACHE_ENABLED,CACHE_TTL_SECONDS) VALUES ('0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf','http://10.207.252.17:8884',1,1000,TIMESTAMP '2026-08-26 04:20:23.192384200 +00:00','Stock management search-isdn-by-stock-model (remote 10.207.252.17:8884)',50,'stock-management-service',5000,1,TIMESTAMP '2026-08-27 08:08:32.148183098 +00:00',0,300);

-- ENDPOINT_CONFIG (2 row)
INSERT INTO ENDPOINT_CONFIG (ID,IS_SEQUENTIAL,CREATED_AT,UPDATED_AT,DESCRIPTION,METHOD,NAME,OUTPUT_ENCODING,PATH) VALUES ('0ad45d66-9dbe-42c0-b460-b0fa57874ff2',1,TIMESTAMP '2026-08-27 11:17:17.851596997 +00:00',TIMESTAMP '2026-08-27 11:17:17.851604421 +00:00','Yeucau yeucau: B1 GET /vcom/search-locked-isdn-by-user?staffCode=... -> B2 product-catalog (fixed params) -> B3 stock-management (body = data B2) -> tra ket qua','GET','Search locked ISDN by user (composite)','json','/vcom/search-locked-isdn-by-user');
INSERT INTO ENDPOINT_CONFIG (ID,IS_SEQUENTIAL,CREATED_AT,UPDATED_AT,DESCRIPTION,METHOD,NAME,OUTPUT_ENCODING,PATH) VALUES ('3b79585a-0d94-45b2-82d2-f1cfcfa97e96',1,TIMESTAMP '2026-08-26 04:21:19.420594200 +00:00',TIMESTAMP '2026-08-26 04:21:19.420594200 +00:00','Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst','POST','Tim ISDN theo Stock Model (composite)','json','/v1/isdn-search-by-stock-model');

-- BACKEND_STEP (4 row)
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('7fe7d102-80b4-41de-bb0b-8d293040b1ff',0,1,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,'GET','Product Catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap',NULL,'53297859-835d-4d06-b4e0-ae3ce51ba943','/product-catalog-service/v1/product/findByTelecomSubTypeOfferTypeCheckProductStatusMap?telecomServiceId=1&offerTypeId=1&getActiveProduct=true',0,300,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('2cf0c588-f8f3-48d4-bdbd-8c9e9ba1cfa1',0,2,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,'POST','Stock Management - search-locked-isdn-by-user',NULL,'d07b5e77-d6eb-41c4-b235-274107f164d9','/stock-management/stock-number/search-locked-isdn-by-user',0,300,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('b90b1f10-b349-440e-bc8f-e150e29071e4',0,1,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,'GET','Catalog - findByTelecomSubTypeOfferType',NULL,'0992a01c-56e6-4de1-8ffb-9e4196ae4eba','/product-catalog-service/v1/product/findByTelecomSubTypeOfferType',0,300,479,166,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO BACKEND_STEP (ID,FORWARD_ORIGINAL_BODY,STEP_ORDER,ENDPOINT_ID,GROUP_NAME,METHOD,NAME,TARGET_FIELD,UPSTREAM_SERVICE_ID,URL_PATTERN,CACHE_ENABLED,CACHE_TTL_SECONDS,CANVAS_X,CANVAS_Y,CONDITION_EXPECTED_VALUE,CONDITION_OPERATOR,CONDITION_SOURCE_FIELD,CONDITION_SOURCE_STEP_ORDER,CONDITION_SOURCE_TYPE,NEXT_STEP_ORDER_IF_FALSE,NEXT_STEP_ORDER_IF_TRUE) VALUES ('1507f489-4566-4e2d-a5b1-4d985c4700e7',0,2,'3b79585a-0d94-45b2-82d2-f1cfcfa97e96',NULL,'POST','Stock - search-isdn-by-stock-model',NULL,'0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf','/stock-management/stock-number/search-isdn-by-stock-model',0,300,483,11,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

-- BACKEND_STEP_ALLOW / DENY / MAPPING: khong co du lieu

-- FIELD_MAPPING (12 row)
INSERT INTO FIELD_MAPPING (ID,SOURCE_STEP_ORDER,TARGET_STEP_ORDER,ENDPOINT_ID,SOURCE_ARRAY_FIELD,SOURCE_ELEMENT_FIELD,SOURCE_FIELD,SOURCE_TYPE,TARGET_PARAM_NAME,TARGET_TYPE,MAPPING_ORDER) VALUES ('d8615ed8-f222-4b0f-a803-dad6d8183bab',NULL,2,'0ad45d66-9dbe-42c0-b460-b0fa57874ff2',NULL,NULL,'staffCode','REQUEST_QUERY','staffCode','QUERY',0);
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

-- ENDPOINT_CONFIG_VERSION (3 row) - SNAPSHOT_JSON (CLOB) duoc chia thanh nhieu dong TO_CLOB(...) || ... 
-- ENDPOINT_CONFIG_VERSION: 1 row (CLOB snapshot chia nho <2499 ky tu/dong cho sqlplus)
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('1a6ce6e8-cc54-409c-a94f-fb0c04a0d0da','CREATED',TIMESTAMP '2026-08-27 11:17:17.949306582 +00:00','0ad45d66-9dbe-42c0-b460-b0fa57874ff2','GET','Search locked ISDN by user (composite)','/vcom/search-locked-isdn-by-user',TO_CLOB('{"id":"0ad45d66-9dbe-42c0-b460-b0fa57874ff2","name":"Search locked ISDN by user (composite)","description":"Yeucau yeucau: B1 GET /vcom/search-locked-isdn-by-user?staffCode=... -> B2 product-catalog (fixed params) -> B3 stock-management (body = data B2) -> tra ket qua","path":"/vcom/search-locked-isdn-by-user","method":"GET","sequential":true,"outputEncoding":"json","steps":[{"id":"7fe7d102-80b4-41de-bb0b-8d293040b1ff","stepOrder":1,"name":"Product Catalog - findByTelecomSubTypeOfferTypeCheckProductStatusMap","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferTypeCheckProductStatusMap?telecomServiceId=1&offerTypeId=1&getActiveProduct=true","upstreamServiceId":"53297859-835d-4d06-b4e0-ae3ce51ba943","upstreamServiceName":"product-catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{}},{"id":"2cf0c588-f8f3-48d4-bdbd-8c9e9ba1cfa1","stepOrder":2,"name":"Stock Mana')
     || TO_CLOB('gement - search-locked-isdn-by-user","method":"POST","urlPattern":"/stock-management/stock-number/search-locked-isdn-by-user","upstreamServiceId":"d07b5e77-d6eb-41c4-b235-274107f164d9","upstreamServiceName":"stock-management","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{}}],"mappings":[{"id":"d8615ed8-f222-4b0f-a803-dad6d8183bab","sourceType":"REQUEST_QUERY","sourceField":"staffCode","targetStepOrder":2,"targetType":"QUERY","targetParamName":"staffCode","mappingOrder":0},{"id":"9a8a95fa-8851-469e-95f9-d26331cd58b7","sourceType":"STEP_RESPONSE","sourceStepOrder":1,"sourceField":"data","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"$body","mappingOrder":1}],"createdAt":"2026-08-27T11:17:17.851596997Z","updatedAt":"2026-08-27T11:17:17.851604421Z"}'),1);
-- ENDPOINT_CONFIG_VERSION: 1 row (CLOB snapshot chia nho <2499 ky tu/dong cho sqlplus)
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('3e3b51e0-0ff5-418f-b306-a9a9cb22cfc7','UPDATED',TIMESTAMP '2026-08-27 06:35:16.464117230 +00:00','3b79585a-0d94-45b2-82d2-f1cfcfa97e96','POST','Tim ISDN theo Stock Model (composite)','/v1/isdn-search-by-stock-model',TO_CLOB('{"id":"3b79585a-0d94-45b2-82d2-f1cfcfa97e96","name":"Tim ISDN theo Stock Model (composite)","description":"Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst","path":"/v1/isdn-search-by-stock-model","method":"POST","sequential":true,"outputEncoding":"json","steps":[{"id":"721073de-4e5b-4cce-a846-7b3a5d0d9099","stepOrder":1,"name":"Catalog - findByTelecomSubTypeOfferType","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferType","upstreamServiceId":"0992a01c-56e6-4de1-8ffb-9e4196ae4eba","upstreamServiceName":"catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":548,"canvasY":244},{"id":"9b256848-c647-4934-a84c-1e3a447c71ba","stepOrder":2,"name":"Stock - search-isdn-by-stock-model","method":"POST","urlPattern":"/stock-management/stock-number/sear')
     || TO_CLOB('ch-isdn-by-stock-model","upstreamServiceId":"0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf","upstreamServiceName":"stock-management-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":893,"canvasY":36}],"mappings":[{"id":"012ff902-6a27-4717-8cf0-480ff646f5a0","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"9d992fde-1d0f-442a-a458-8df7378f29fa","sourceType":"REQUEST_BODY","sourceField":"subType","targetStepOrder":1,"targetType":"QUERY","targetParamName":"subType","mappingOrder":0},{"id":"132e6db8-76e5-4f30-b84e-c43f66e3f98d","sourceType":"REQUEST_BODY","sourceField":"offerTypeId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"offerTypeId","mappingOrder":0},{"id":"0aedef7b-858c-4d64-82f9-617654247d46","sourceType":"STEP_RESPONSE_ARRAY_AGGREGATE","sourceStepOrder":1,"sourceArra')
     || TO_CLOB('yField":"data","sourceElementField":"productOfferingId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"stockModelIds","mappingOrder":0},{"id":"4bc4e9cd-5fdf-4150-bb2a-5a0588494080","sourceType":"REQUEST_BODY","sourceField":"pageSize","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"pageSize","mappingOrder":0},{"id":"4eb2133d-e6a7-4688-8a5b-3fc6aa22e50f","sourceType":"REQUEST_BODY","sourceField":"typeIsdn","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"typeIsdn","mappingOrder":0},{"id":"f57ef7ca-28dd-4a78-ba1c-390b60856586","sourceType":"REQUEST_BODY","sourceField":"shopIds","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"shopIds","mappingOrder":0},{"id":"ed2f4548-09de-41ee-a9e1-7b1c16946faa","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"fe06aa0b-a4bc-494b-9749-8c31b9a19ca5","sourceType":"REQUEST_B')
     || TO_CLOB('ODY","sourceField":"page","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"page","mappingOrder":0},{"id":"56d114dd-d3d8-40f8-a0da-a0483bb19cb3","sourceType":"REQUEST_BODY","sourceField":"staffCode","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"staffCode","mappingOrder":0}],"createdAt":"2026-08-26T04:21:19.420594200Z","updatedAt":"2026-08-26T04:21:19.420594200Z"}'),1);
-- ENDPOINT_CONFIG_VERSION: 1 row (CLOB snapshot chia nho <2499 ky tu/dong cho sqlplus)
INSERT INTO ENDPOINT_CONFIG_VERSION (ID,CHANGE_TYPE,CREATED_AT,ENDPOINT_ID,METHOD,NAME,PATH,SNAPSHOT_JSON,VERSION_NUMBER) VALUES ('4256ad24-42b7-4234-a22e-f5dc2fe12a96','UPDATED',TIMESTAMP '2026-08-27 06:35:28.646321861 +00:00','3b79585a-0d94-45b2-82d2-f1cfcfa97e96','POST','Tim ISDN theo Stock Model (composite)','/v1/isdn-search-by-stock-model',TO_CLOB('{"id":"3b79585a-0d94-45b2-82d2-f1cfcfa97e96","name":"Tim ISDN theo Stock Model (composite)","description":"Goi catalog de lay ds product offering -> gom code thanh prodOfferCodeLst -> goi stock-management search-isdn-by-stock-model voi body goc + prodOfferCodeLst","path":"/v1/isdn-search-by-stock-model","method":"POST","sequential":true,"outputEncoding":"json","steps":[{"id":"b90b1f10-b349-440e-bc8f-e150e29071e4","stepOrder":1,"name":"Catalog - findByTelecomSubTypeOfferType","method":"GET","urlPattern":"/product-catalog-service/v1/product/findByTelecomSubTypeOfferType","upstreamServiceId":"0992a01c-56e6-4de1-8ffb-9e4196ae4eba","upstreamServiceName":"catalog-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":479,"canvasY":166},{"id":"1507f489-4566-4e2d-a5b1-4d985c4700e7","stepOrder":2,"name":"Stock - search-isdn-by-stock-model","method":"POST","urlPattern":"/stock-management/stock-number/sear')
     || TO_CLOB('ch-isdn-by-stock-model","upstreamServiceId":"0b6a43a4-3e2d-41f6-96c8-74d9d8b26aaf","upstreamServiceName":"stock-management-service","forwardOriginalBody":false,"cacheEnabled":false,"cacheTtlSeconds":300,"allowFields":[],"denyFields":[],"fieldRenameMapping":{},"canvasX":483,"canvasY":11}],"mappings":[{"id":"910528f4-d7f9-4f04-bb35-ac38c185ebcc","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"24b30320-fcd4-4e77-ac8c-a8e265ec3129","sourceType":"REQUEST_BODY","sourceField":"subType","targetStepOrder":1,"targetType":"QUERY","targetParamName":"subType","mappingOrder":0},{"id":"0e165292-3fde-409e-b67b-29712d747253","sourceType":"REQUEST_BODY","sourceField":"offerTypeId","targetStepOrder":1,"targetType":"QUERY","targetParamName":"offerTypeId","mappingOrder":0},{"id":"6c0cde5a-ccaf-471f-8209-fc6ad3f97be0","sourceType":"STEP_RESPONSE_ARRAY_AGGREGATE","sourceStepOrder":1,"sourceArra')
     || TO_CLOB('yField":"data","sourceElementField":"productOfferingId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"stockModelIds","mappingOrder":0},{"id":"62a6e27c-e2b7-4021-b2f8-8d1544a895de","sourceType":"REQUEST_BODY","sourceField":"staffCode","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"staffCode","mappingOrder":0},{"id":"8343453a-540e-48c5-8556-9c25cdd207f4","sourceType":"REQUEST_BODY","sourceField":"typeIsdn","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"typeIsdn","mappingOrder":0},{"id":"c19cdf93-3cfb-43e4-becf-6a4e93e4164e","sourceType":"REQUEST_BODY","sourceField":"shopIds","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"shopIds","mappingOrder":0},{"id":"dc4b7682-3b13-458b-ab82-3cce2226b942","sourceType":"REQUEST_BODY","sourceField":"telecomServiceId","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"telecomServiceId","mappingOrder":0},{"id":"d3f0829f-3575-41c8-900e-564d4dc8d252","sourceType":"REQUEST')
     || TO_CLOB('_BODY","sourceField":"page","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"page","mappingOrder":0},{"id":"efdd274a-d370-46f8-a6b4-6f95c2ef0ba7","sourceType":"REQUEST_BODY","sourceField":"pageSize","targetStepOrder":2,"targetType":"BODY_FIELD","targetParamName":"pageSize","mappingOrder":0}],"createdAt":"2026-08-26T04:21:19.420594200Z","updatedAt":"2026-08-26T04:21:19.420594200Z"}'),2);

-- ============================================================================
-- HET FILE gateway-manager-dump.sql
-- ============================================================================
