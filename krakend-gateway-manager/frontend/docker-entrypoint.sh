#!/bin/sh
# ============================================================================
# Thay "__DNS_RESOLVER__" trong nginx.conf.template bang nameserver DAU TIEN
# doc tu /etc/resolv.conf cua CHINH container/pod nay truoc khi khoi dong nginx
# - tren Docker Compose do la DNS noi bo cua Docker (127.0.0.11), tren
# Kubernetes do la ClusterIP thuc cua CoreDNS/kube-dns (khac nhau giua tung
# cum, khong the hardcode 1 gia tri chung - xem nginx.conf.template).
# ============================================================================
set -e

DNS_RESOLVER=$(awk '/^nameserver/{print $2; exit}' /etc/resolv.conf)
if [ -z "$DNS_RESOLVER" ]; then
    echo "canh bao: khong doc duoc nameserver tu /etc/resolv.conf, fallback ve 127.0.0.11" >&2
    DNS_RESOLVER="127.0.0.11"
fi

sed "s/__DNS_RESOLVER__/${DNS_RESOLVER}/" /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
