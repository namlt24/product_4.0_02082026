-- ============================================================================
-- Cho phep tach Bulkhead (Resilience4j) RIENG theo tung BACKEND_STEP - mac
-- dinh MOI step van dung CHUNG Bulkhead cua UPSTREAM_SERVICE (hanh vi cu,
-- khong doi khi 2 cot moi duoi day = NULL). Xem BackendStep.maxConcurrentCallsOverride,
-- UpstreamHttpExecutor.bulkheadFor() ve y nghia/danh doi day du - mirror pattern
-- "mac dinh dung chung theo backend + override co chon loc theo route/subset"
-- cua Istio DestinationRule/Envoy priority bucket.
--
-- QUY TRINH BAN GIAO GIONG HET V1/V2 (xem huong dan day du trong V1__baseline.sql) -
-- gui file NAY cho DBA cua doi NEN TANG, DBA tu chay tren schema da co san 8
-- bang (V1) + GWM_TEAM (V2), sau do cap lai quyen DML cho user RUNTIME (khong
-- doi gi ve quyen han).
--
-- Ca 2 cot deu NULLABLE, KHONG DEFAULT - an toan them vao bang da co du lieu
-- (moi step hien co tu dong = NULL = tiep tuc dung Bulkhead chung cua Upstream).
-- ============================================================================

ALTER TABLE "BACKEND_STEP" ADD "MAX_CONCURRENT_CALLS_OVERRIDE" NUMBER(10,0);
ALTER TABLE "BACKEND_STEP" ADD "MAX_WAIT_DURATION_MS_OVERRIDE" NUMBER(10,0);
