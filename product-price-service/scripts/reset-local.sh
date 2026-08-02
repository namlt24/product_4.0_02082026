#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$APP_ROOT/docker-compose.local.yml"
ENV_FILE="$APP_ROOT/.env"

info() { printf '[INFO] %s\n' "$*"; }
ok() { printf '[OK] %s\n' "$*"; }
error() { printf '[ERROR] %s\n' "$*" >&2; }

compose() {
  docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" "$@"
}

if [ ! -f "$ENV_FILE" ]; then
  error "Missing .env. Copy .env.example to .env before resetting local dependencies."
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  error "Docker CLI is not installed or not on PATH."
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  error "Docker daemon is not running. Start Docker Desktop or your Docker engine, then retry."
  exit 1
fi

cat <<'EOF'
This will stop local dependencies and remove named Docker volumes for this service.
All local MySQL, Redis, and Kafka data in the compose volumes will be deleted.
EOF

printf 'Type "reset" to continue: '
read -r CONFIRMATION

if [ "$CONFIRMATION" != "reset" ]; then
  info "Reset cancelled."
  exit 0
fi

compose down -v
ok "Local dependency volumes removed."

if [ "${RESTART_LOCAL_DEPS:-true}" = "true" ]; then
  info "Restarting local dependencies..."
  compose up -d
  ok "Local dependencies restarted."
else
  info "Skipped restart because RESTART_LOCAL_DEPS is not true."
fi
