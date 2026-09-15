#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
audit_vs_config.py — Báo cáo "P99/rps thực tế vs cấu hình resilience" của 1
Data Plane đội. Nguồn số liệu: audit hop đã ghi sẵn trong ES (gwm-hops-*) +
cấu hình Upstream hiện tại trên Control Plane (GET /api/upstreams).

CÔNG THỨC kiểm soát (xem PRODUCTION_RUNBOOK.md — mục kiểm soát vòng đời
cấu hình resilience):
  R1  circuitBreakerEnabled=true bắt buộc
  R2  readTimeoutMs > P99  (P99 >= timeout = timeout ảo)
  R3  maxConcurrentCalls >= P99(giây) × rps × 1.2  (lệch > 30% = lệch)
  R4  retryEnabled=false khi P99 > 2000ms (retry vào upstream chậm = bão ×3)
  R5  Circuit breaker đang OPEN (kiểm tra qua kubectl, bật khi có NS + KUBECONFIG)

CHỈ ĐỌC — không gọi API ghi nào, an toàn chạy tự động (cron).
Yêu cầu: Python 3.8+, thư viện urllib có sẵn (KHÔNG cần pip install gì).

Cách chạy: xem hàm main() / `python3 audit_vs_config.py --help`.
Exit code: 0 = ổn, 1 = có cảnh báo, 2 = lỗi cấu hình/kết nối.
"""
import argparse
import datetime as dt
import io
import json
import sys
import urllib.error
import urllib.request

# Windows console mặc định cp1252 — không in được tiếng Việt
if sys.stdout.encoding and sys.stdout.encoding.lower() not in ("utf-8", "utf8"):
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")


def http_json(url, payload=None, headers=None, timeout=30):
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(url, data=data, headers=headers or {})
    if data:
        req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode())


def query_hops(es_url, days):
    """Tổng hợp audit hop theo upstream: P99, số call, lỗi, rps."""
    index = f"gwm-hops-{(dt.date.today() - dt.timedelta(days=days)).strftime('%Y.%m.%d')}*,gwm-hops-{dt.date.today().strftime('%Y.%m.%d')}*"
    query = {
        "size": 0,
        "query": {"range": {"timestamp": {"gte": f"now-{days}d/d", "lte": "now"}}},
        "aggs": {
            "by_upstream": {
                "terms": {"field": "upstreamName.keyword", "size": 100},
                "aggs": {
                    "p99": {"percentiles": {"field": "durationMs", "percents": [99]}},
                    "calls": {"value_count": {"field": "durationMs"}},
                    "errors": {"filter": {"term": {"success": False}}},
                },
            }
        },
    }
    resp = http_json(f"{es_url.rstrip('/')}/{index}/_search", query)
    out = {}
    for b in resp.get("aggregations", {}).get("by_upstream", {}).get("buckets", []):
        calls = b["calls"]["value"]
        out[b["key"]] = {
            "p99_ms": b["p99"]["values"].get("99.0", 0.0),
            "calls": calls,
            "errors": b["errors"]["doc_count"],
            "rps": round(calls / (days * 86400), 2),
        }
    return out


def fetch_config(cp_url, team_key):
    cfgs = http_json(
        f"{cp_url.rstrip('/')}/api/upstreams", headers={"X-Gateway-Admin-Key": team_key}
    )
    if not isinstance(cfgs, list):
        raise RuntimeError(f"Control Plane trả về không đúng mảng: {str(cfgs)[:200]}")
    return {u["name"]: u for u in cfgs}


def cb_open_states(ns):
    """Trả về set upstream đang OPEN/FORCED_OPEN — cần kubectl. Trả None nếu không chạy được."""
    import subprocess
    try:
        raw = subprocess.run(
            ["kubectl", "exec", "-n", ns, "deploy/gwm-backend", "-c", "backend", "--",
             "curl", "-s", "--max-time", "8", "localhost:8080/actuator/circuitbreakers"],
            capture_output=True, text=True, timeout=30,
        ).stdout
        data = json.loads(raw)
        cbs = data.get("circuitBreakers", data)
        if isinstance(cbs, dict):
            items = cbs.items()
        else:  # một số version trả list [{name, state}]
            items = [(c.get("name"), c) for c in cbs]
        return {k for k, v in items if isinstance(v, dict) and v.get("state") in ("OPEN", "FORCED_OPEN")}
    except Exception:
        return None


def main():
    ap = argparse.ArgumentParser(
        description="P99/rps thực tế (audit ES) vs cấu hình resilience (Control Plane)",
        formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--es-url", default="http://elasticsearch.myviettel.svc.cluster.local:9200")
    ap.add_argument("--cp-url", default="http://10.29.1.21:8051")
    ap.add_argument("--team-key", required=True, help="api_key của đội (X-Gateway-Admin-Key)")
    ap.add_argument("--days", type=int, default=7, help="tổng hợp N ngày gần nhất (mặc định 7)")
    ap.add_argument("--threshold", type=int, default=30, help="ngưỡng %% lệch bulkhead để báo (mặc định 30)")
    ap.add_argument("--ns", help="namespace k8s của đội — bật kiểm tra CB OPEN qua kubectl")
    args = ap.parse_args()

    try:
        hops = query_hops(args.es_url, args.days)
    except Exception as e:
        print(f"LOI: không truy vấn được ES ({e}) — kiểm tra --es-url", file=sys.stderr)
        return 2
    try:
        cfg = fetch_config(args.cp_url, args.team_key)
    except Exception as e:
        print(f"LOI: không lấy được cấu hình từ Control Plane ({e}) — kiểm tra --cp-url/--team-key", file=sys.stderr)
        return 2

    if not hops:
        print(f"Không có hop audit nào trong {args.days} ngày qua — audit chưa bật hoặc chưa có traffic.")
        return 0

    print(f"=== P99/rps thực tế (audit {args.days} ngày) vs cấu hình Upstream (Control Plane) ===")
    hdr = f"{'UPSTREAM':<22} {'P99(ms)':>9} {'rps':>7} {'err%':>5} {'calls':>8}  {'readTO':>7} {'bulkhead':>8} {'retry':>5} {'cb':>4}"
    print(hdr)
    print("-" * len(hdr))

    names = sorted(set(hops) | set(cfg))
    rows = []
    for n in names:
        h = hops.get(n, {})
        c = cfg.get(n, {})
        rows.append({
            "name": n,
            "p99": h.get("p99_ms", 0),
            "rps": h.get("rps", 0),
            "errpct": (h.get("errors", 0) * 100 / h["calls"]) if h.get("calls") else None,
            "calls": h.get("calls", 0),
            "to": c.get("readTimeoutMs"),
            "bh": c.get("maxConcurrentCalls"),
            "retry": c.get("retryEnabled"),
            "cb": c.get("circuitBreakerEnabled"),
        })

    for r in rows:
        errpct = f"{r['errpct']:.0f}%" if r["errpct"] is not None else "-"
        retry_s = "on" if r["retry"] else ("off" if r["retry"] is not None else "-")
        cb_s = "on" if r["cb"] else ("OFF" if r["cb"] is not None else "-")
        p99_s = f"{r['p99']:.0f}" if r["p99"] else "-"
        print(f"{r['name']:<22} {p99_s:>9} {r['rps']:>7} {errpct:>5} "
              f"{r['calls']:>8}  {r['to'] or '-':>7} {r['bh'] or '-':>8} {retry_s:>5} {cb_s:>4}")

    print()
    warnings = 0
    for r in rows:
        p99, to, bh = r["p99"], r["to"], r["bh"]
        if r["cb"] is False:
            print(f"[R1-NGHIEM] upstream '{r['name']}': circuitBreakerEnabled=false — vi phạm qui tắc bắt buộc")
            warnings += 1
        if p99 and to and p99 >= to:
            print(f"[R2] upstream '{r['name']}': P99 {p99:.0f}ms >= readTimeoutMs {to}ms — timeout ảo, đo lại và chỉnh")
            warnings += 1
        if p99 and bh and r["rps"] > 0:
            need = (p99 / 1000) * r["rps"] * 1.2
            if need > 0 and bh > 0:
                diff = (need - bh) / bh * 100
                if diff > args.threshold:
                    print(f"[R3] upstream '{r['name']}': bulkhead cần ~{need:.0f} (P99 {p99:.0f}ms × {r['rps']}rps × 1.2) "
                          f"nhưng đang set {bh} ({diff:+.0f}%)")
                    warnings += 1
        if r["retry"] and p99 > 2000:
            print(f"[R4] upstream '{r['name']}': retryEnabled=on nhưng P99 {p99:.0f}ms > 2s — nên tắt retry (bão tải ×3 khi chậm)")
            warnings += 1

    if args.ns:
        opened = cb_open_states(args.ns)
        if opened is None:
            print("(không kiểm tra được /actuator/circuitbreakers qua kubectl — bỏ qua R5)")
        elif opened:
            print(f"[R5] Circuit breaker đang OPEN: {', '.join(sorted(opened))} — kiểm tra ngay")
            warnings += 1

    print()
    if warnings:
        print(f"===> {warnings} cảnh báo — xử lý theo runbook (mục kiểm soát cấu hình resilience).")
        return 1
    print(f"OK: không có lệch vượt ngưỡng {args.threshold}% trong {args.days} ngày qua.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
