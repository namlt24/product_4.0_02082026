-- ============================================================================
-- Nang cap len multi-tenant theo TEAM_CODE (2026-09): Control Plane + DB gio
-- DUNG CHUNG cho MOI doi BCCS (khac V1__baseline.sql - luc do moi doi 1 DB
-- rieng hoan toan). File nay them cot TEAM_CODE + bang GWM_TEAM de cach ly
-- du lieu giua cac doi tren CUNG 1 schema.
--
-- QUY TRINH BAN GIAO GIONG HET V1 (xem huong dan day du trong file do) - gui
-- file NAY cho DBA cua doi NEN TANG (khong phai tung doi con lai - Control
-- Plane gio chi co 1 instance duy nhat, dung chung), DBA tu chay tren schema
-- da co san 8 bang cua V1, sau do cap lai quyen DML cho user RUNTIME (khong
-- doi gi ve quyen han).
--
-- QUAN TRONG: chay file nay se dong thoi XOA UNIQUE("NAME")/UNIQUE("PATH")
-- toan cuc cu tren UPSTREAM_SERVICE/ENDPOINT_CONFIG, thay bang UNIQUE theo
-- CAP (TEAM_CODE, NAME)/(TEAM_CODE, PATH) - tu sau buoc nay, 2 doi khac nhau
-- DUOC PHEP dat trung ten Upstream/trung path Endpoint (truoc day KHONG duoc,
-- vi la 1 DB rieng/doi nen chua bao gio can phan biet).
-- ============================================================================

-- 1) Bang GWM_TEAM - danh sach doi dang dung chung Control Plane nay.
CREATE TABLE "GWM_TEAM"
   (	"TEAM_CODE" VARCHAR2(50 CHAR) NOT NULL ENABLE,
	"TEAM_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"API_KEY" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	"CREATED_AT" TIMESTAMP (6) WITH TIME ZONE,
	 CONSTRAINT "GWM_TEAM_PK" PRIMARY KEY ("TEAM_CODE") ENABLE,
	 CONSTRAINT "GWM_TEAM_API_KEY_UK" UNIQUE ("API_KEY") ENABLE
   );

-- 2) Them TEAM_CODE (backfill 'default' cho du lieu demo/cu dang co san,
--    khop dung gia tri fallback TEAM_CODE:-default da dung san trong
--    docker-compose/k8s) - 3 buoc ADD (cho phep NULL) -> UPDATE backfill ->
--    MODIFY NOT NULL, tranh loi "cannot insert NULL" giua chung khi bang da
--    co du lieu.
ALTER TABLE "UPSTREAM_SERVICE" ADD "TEAM_CODE" VARCHAR2(50 CHAR);
UPDATE "UPSTREAM_SERVICE" SET "TEAM_CODE" = 'default' WHERE "TEAM_CODE" IS NULL;
ALTER TABLE "UPSTREAM_SERVICE" MODIFY "TEAM_CODE" NOT NULL;

ALTER TABLE "ENDPOINT_CONFIG" ADD "TEAM_CODE" VARCHAR2(50 CHAR);
UPDATE "ENDPOINT_CONFIG" SET "TEAM_CODE" = 'default' WHERE "TEAM_CODE" IS NULL;
ALTER TABLE "ENDPOINT_CONFIG" MODIFY "TEAM_CODE" NOT NULL;

-- 3) Doi UNIQUE toan cuc -> UNIQUE theo cap (TEAM_CODE, NAME|PATH). Ten
--    constraint cu ("UKR9K2UNQ519ISGPHWC5EMCHYUC"/"UK5SBR9SP37R2WRGTA6BTBEE3XB")
--    tra ve dung tu V1__baseline.sql - sua lai neu DBA cua doi da doi ten
--    khac luc chay V1.
ALTER TABLE "UPSTREAM_SERVICE" DROP CONSTRAINT "UKR9K2UNQ519ISGPHWC5EMCHYUC";
ALTER TABLE "UPSTREAM_SERVICE" ADD CONSTRAINT "UPSTREAM_SERVICE_TEAM_NAME_UK" UNIQUE ("TEAM_CODE", "NAME") ENABLE;

ALTER TABLE "ENDPOINT_CONFIG" DROP CONSTRAINT "UK5SBR9SP37R2WRGTA6BTBEE3XB";
ALTER TABLE "ENDPOINT_CONFIG" ADD CONSTRAINT "ENDPOINT_CONFIG_TEAM_PATH_UK" UNIQUE ("TEAM_CODE", "PATH") ENABLE;

-- 4) Tao doi "default" (khop du lieu demo/cu vua backfill o buoc 2) - PHAI
--    thay <mat-khau-that-cua-GATEWAY_ADMIN_API_KEY-cu> bang chinh gia tri
--    GATEWAY_ADMIN_API_KEY dang dung TRUOC KHI nang cap (de instance cu tiep
--    tuc dang nhap duoc bang key quen thuoc) - KHONG duoc de nguyen placeholder
--    nay khi chay that. Sau buoc nang cap, gia tri nay tro thanh apiKey CUA
--    RIENG doi "default" (khong con la platform-admin key nua - platform-admin
--    key van la GATEWAY_ADMIN_API_KEY hien tai, khong doi gi ca, chi thay doi
--    Y NGHIA tu "1 key duy nhat cho tat ca" thanh "key rieng cho tac vu quan
--    tri doi", xem SAD ADR-07).
INSERT INTO "GWM_TEAM" ("TEAM_CODE", "TEAM_NAME", "API_KEY", "CREATED_AT")
VALUES ('default', 'Doi mac dinh (du lieu truoc khi co multi-tenant)', '<mat-khau-that-cua-GATEWAY_ADMIN_API_KEY-cu>', SYSTIMESTAMP);
