package com.bccs.gatewaymanager.audit;

/**
 * Cat body request/response truoc khi day vao Elasticsearch - tranh document
 * qua lon (anh huong hieu nang bulk index/tim kiem) va tranh "mapping explosion"
 * neu body chua cau truc JSON bat thuong. Gioi han 8KB la du de xem chi tiet
 * debug hau het truong hop thuc te ma khong lam index phinh to vo ich.
 */
public final class BodyTruncator {

    public static final int MAX_LENGTH = 8000;

    private BodyTruncator() {
    }

    public record Result(String body, boolean truncated) {
    }

    public static Result truncate(String raw) {
        if (raw == null) {
            return new Result(null, false);
        }
        if (raw.length() <= MAX_LENGTH) {
            return new Result(raw, false);
        }
        return new Result(raw.substring(0, MAX_LENGTH), true);
    }
}
