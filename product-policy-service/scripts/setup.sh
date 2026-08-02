#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$APP_ROOT/docker-compose.local.yml"
ENV_FILE="$APP_ROOT/.env"
ENV_EXAMPLE="$APP_ROOT/.env.example"

info() { printf '[INFO] %s\n' "$*"; }
ok() { printf '[OK] %s\n' "$*"; }
warn() { printf '[WARN] %s\n' "$*" >&2; }
error() { printf '[ERROR] %s\n' "$*" >&2; }

compose() {
  docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" "$@"
}

load_env() {
  if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    set +a
  fi
}

require_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    error "Docker CLI is not installed or not on PATH."
    exit 1
  fi

  if ! docker info >/dev/null 2>&1; then
    error "Docker daemon is not running. Start Docker Desktop or your Docker engine, then retry."
    exit 1
  fi

  ok "Docker daemon is running."
}

find_build_wrapper() {
  if [ -x "$APP_ROOT/mvnw" ]; then
    printf '%s\n' "$APP_ROOT/mvnw"
    return 0
  fi

  if [ -x "$APP_ROOT/gradlew" ]; then
    printf '%s\n' "$APP_ROOT/gradlew"
    return 0
  fi

  return 1
}

run_tests() {
  if ! find_build_wrapper >/dev/null 2>&1; then
    error "No Maven or Gradle wrapper found. Add mvnw or gradlew to the generated service before running setup."
    exit 1
  fi

  "$SCRIPT_DIR/test.sh"
}

wait_for_database() {
  info "Waiting for Oracle..."
  for _ in $(seq 1 30); do
    if compose exec -T database healthcheck.sh >/dev/null 2>&1; then
      ok "Oracle is ready."
      return 0
    fi
    sleep 2
  done

  error "Oracle did not become ready in time."
  exit 1
}

wait_for_redis() {
  info "Waiting for Redis..."
  for _ in $(seq 1 30); do
    if compose exec -T redis redis-cli ping >/dev/null 2>&1; then
      ok "Redis is ready."
      return 0
    fi
    sleep 2
  done

  error "Redis did not become ready in time."
  exit 1
}

run_migrations_if_configured() {
  if [ -n "${DATABASE_MIGRATION_COMMAND:-}" ]; then
    info "Running configured database migration command..."
    (cd "$APP_ROOT" && sh -c "$DATABASE_MIGRATION_COMMAND")
    ok "Database migration command completed."
  else
    warn "No DATABASE_MIGRATION_COMMAND configured; skipping database migration."
  fi
}

print_urls() {
  cat <<EOF

Local URLs:
  Service:   http://localhost:${APP_PORT:-8080}
  Actuator:  http://localhost:${APP_PORT:-8080}/actuator
  Kafka UI:  http://localhost:${KAFKA_UI_PORT:-8085}

Local dependencies:
  Oracle:     localhost:${DB_PORT:-1521}/${DB_NAME:-FREEPDB1}
  Redis:      localhost:${REDIS_PORT:-6379}
  Kafka:      ${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}
EOF
}

cd "$APP_ROOT"

if [ ! -f "$ENV_FILE" ]; then
  if [ ! -f "$ENV_EXAMPLE" ]; then
    error "Missing .env.example; cannot create .env."
    exit 1
  fi
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  ok "Created .env from .env.example."
fi

load_env
require_docker

info "Starting local dependencies..."
compose up -d
wait_for_database
wait_for_redis
run_migrations_if_configured
run_tests
print_urls
