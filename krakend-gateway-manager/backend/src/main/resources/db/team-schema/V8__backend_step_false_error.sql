-- ============================================================================
-- V8 - Nhanh SAI cua rẽ nhánh (2 nhánh / switch) co the BAO LOI thay vi chuyen step:
--      FALSE_ERROR_CODE (ma loi), FALSE_ERROR_MESSAGE (thong diep). Ca 2 nullable,
--      step cu = NULL = hanh vi nhu truoc. Quy trinh ban giao DBA giong V1..V7.
-- Chay 1 lan: 2 cot phai chua ton tai.
-- ============================================================================

ALTER TABLE "BACKEND_STEP" ADD ("FALSE_ERROR_CODE" VARCHAR2(60), "FALSE_ERROR_MESSAGE" VARCHAR2(500));
COMMIT;
