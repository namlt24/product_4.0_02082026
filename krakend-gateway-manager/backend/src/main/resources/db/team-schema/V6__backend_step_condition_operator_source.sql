-- ============================================================================
-- V6 - Mo rong CHECK cua BACKEND_STEP.CONDITION_OPERATOR va
-- BACKEND_STEP.CONDITION_SOURCE_TYPE cho cac toan tu/nguon dieu kien moi:
--   operator: CONTAINS, NOT_CONTAINS, IN, NOT_IN, STARTS_WITH, ENDS_WITH,
--             STRING_CONTAINS, IS_EMPTY, IS_NOT_EMPTY, MATCHES_REGEX
--   source:   QUERY_PARAM, HEADER, SYSTEM_FLAG
--
-- V1__baseline.sql tao CHECK ma KHONG dat ten (he thong sinh SYS_C0xxxxx, ten
-- khac nhau giua cac moi truong), nen migration nay TIM constraint theo noi dung
-- cua no roi moi DROP - KHONG drop theo ten cu the.
--
-- Quy trinh ban giao DBA giong V1/V2/V3/V5: gui file nay cho DBA doi nen tang,
-- chay tren schema da co V1..V5. Chay lai duoc an toan (neu da mo rong roi thi
-- buoc tim/drop khong tim thay gi, buoc ADD ten moi se bao loi - khi do bo qua
-- phan ADD vi da co).
-- ============================================================================

BEGIN
  FOR r IN (
    SELECT constraint_name
      FROM user_constraints
     WHERE table_name = 'BACKEND_STEP'
       AND constraint_type = 'C'
       AND (search_condition_vc LIKE '%condition_operator in%'
            OR search_condition_vc LIKE '%condition_source_type in%')
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE "BACKEND_STEP" DROP CONSTRAINT "' || r.constraint_name || '"';
  END LOOP;
END;
/

ALTER TABLE "BACKEND_STEP" ADD CONSTRAINT "BACKEND_STEP_COND_OP_CHK" CHECK (condition_operator IN (
  'EQUALS','NOT_EQUALS','EXISTS','NOT_EXISTS',
  'GREATER_THAN','GREATER_THAN_OR_EQUAL','LESS_THAN','LESS_THAN_OR_EQUAL',
  'CONTAINS','NOT_CONTAINS',
  'IN','NOT_IN',
  'STARTS_WITH','ENDS_WITH','STRING_CONTAINS',
  'IS_EMPTY','IS_NOT_EMPTY',
  'MATCHES_REGEX'
));

ALTER TABLE "BACKEND_STEP" ADD CONSTRAINT "BACKEND_STEP_COND_SRC_CHK" CHECK (condition_source_type IN (
  'STEP_RESPONSE','REQUEST_BODY','STEP_RESPONSE_ARRAY_AGGREGATE',
  'QUERY_PARAM','HEADER','SYSTEM_FLAG'
));
