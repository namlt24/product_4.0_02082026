-- ============================================================================
-- V9 - FieldMapping nguon moi STEP_RESPONSE_ARRAY_FIND: tim PHAN TU DAU TIEN
-- trong 1 mang response step truoc co 1 field (filter_field) khop VAN BAN voi
-- 1 gia tri (filter_value), roi trich field khac (source_element_field, cot da
-- co tu truoc, tai su dung dung quy uoc STEP_RESPONSE_ARRAY_AGGREGATE) tu DUNG
-- phan tu khop do - xem javadoc FieldMappingSourceType.STEP_RESPONSE_ARRAY_FIND.
--
-- 2 cot moi FILTER_FIELD/FILTER_VALUE: nullable, chi co y nghia khi
-- source_type=STEP_RESPONSE_ARRAY_FIND (giong cach SOURCE_ARRAY_FIELD/
-- SOURCE_ELEMENT_FIELD chi co y nghia voi STEP_RESPONSE_ARRAY_AGGREGATE).
--
-- FIELD_MAPPING_SOURCE_TYPE_CHK (V1__baseline.sql) duoc dat ten ro rang (khac
-- CHECK khong ten cua BACKEND_STEP ma V6 phai tim theo noi dung) nen DROP
-- truc tiep theo ten roi ADD lai voi gia tri moi.
--
-- Quy trinh ban giao DBA giong V1..V8. Chay lai duoc an toan: neu 2 cot da
-- ton tai buoc ADD COLUMN se loi ro rang (bo qua buoc do khi chay lai); buoc
-- DROP CONSTRAINT tim theo ten cu the nen neu da doi ten roi se loi ro rang
-- tuong tu, khong am tham bo qua.
-- ============================================================================

ALTER TABLE "FIELD_MAPPING" ADD ("FILTER_FIELD" VARCHAR2(255 CHAR), "FILTER_VALUE" VARCHAR2(255 CHAR));

ALTER TABLE "FIELD_MAPPING" DROP CONSTRAINT "FIELD_MAPPING_SOURCE_TYPE_CHK";

ALTER TABLE "FIELD_MAPPING" ADD CONSTRAINT "FIELD_MAPPING_SOURCE_TYPE_CHK" CHECK (source_type IN (
  'STEP_RESPONSE','REQUEST_BODY','QUERY_PARAM','STEP_RESPONSE_ARRAY_AGGREGATE',
  'CONSTANT','STEP_RESPONSE_ARRAY_MERGE','STEP_RESPONSE_ARRAY_FIND'
)) ENABLE;

COMMIT;
