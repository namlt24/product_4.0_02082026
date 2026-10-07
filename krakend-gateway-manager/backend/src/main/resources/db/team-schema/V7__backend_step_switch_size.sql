-- ============================================================================
-- V7 - (1) Cot SWITCH_BRANCHES (CLOB JSON) cho step RE NHIEU NHANH (switch);
--      (2) mo rong CHECK CONDITION_OPERATOR them toan tu KICH THUOC:
--          SIZE_EQUALS, SIZE_GREATER_THAN, SIZE_GREATER_THAN_OR_EQUAL,
--          SIZE_LESS_THAN, SIZE_LESS_THAN_OR_EQUAL.
-- Lap mang (ANY/ALL/COUNT) khong can cot moi: nam trong CONDITION_GROUPS (JSON, V6).
--
-- Quy trinh ban giao DBA giong V1..V6. Nullable CLOB, khong DEFAULT - step cu = NULL
-- = dung 2 nhanh nhu truoc, khong doi hanh vi.
-- ============================================================================

-- Chay 1 lan: cot SWITCH_BRANCHES phai chua ton tai.
ALTER TABLE "BACKEND_STEP" ADD "SWITCH_BRANCHES" CLOB;

BEGIN
  FOR r IN (
    SELECT constraint_name
      FROM user_constraints
     WHERE table_name = 'BACKEND_STEP'
       AND constraint_type = 'C'
       AND UPPER(search_condition_vc) LIKE '%CONDITION_OPERATOR IN%'
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
  'MATCHES_REGEX',
  'SIZE_EQUALS','SIZE_GREATER_THAN','SIZE_GREATER_THAN_OR_EQUAL','SIZE_LESS_THAN','SIZE_LESS_THAN_OR_EQUAL'
));
