#!/usr/bin/env bash
set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$APP_ROOT/docker-compose.local.yml"
ENV_FILE="$APP_ROOT/.env"

ERRORS=0
WARNINGS=0

ok() { printf '[OK] %s\n' "$*"; }
warn() { WARNINGS=$((WARNINGS + 1)); printf '[WARN] %s\n' "$*"; }
error() { ERRORS=$((ERRORS + 1)); printf '[ERROR] %s\n' "$*"; }

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

check_command() {
  local command_name="$1"
  local label="$2"
  if command -v "$command_name" >/dev/null 2>&1; then
    ok "$label is available."
  else
    error "$label is not installed or not on PATH."
  fi
}

check_port() {
  local port="$1"
  local label="$2"
  if command -v nc >/dev/null 2>&1; then
    if nc -z localhost "$port" >/dev/null 2>&1; then
      ok "$label port $port is reachable on localhost."
    else
      warn "$label port $port is not reachable. Start local dependencies if this service should be running."
    fi
  else
    warn "Cannot check $label port $port because nc is not available."
  fi
}

check_compose_service() {
  local service="$1"
  if compose ps "$service" >/dev/null 2>&1; then
    ok "Compose service '$service' is known."
  else
    warn "Compose service '$service' is not available yet. Run scripts/setup.sh to start dependencies."
  fi
}

load_env

check_command java "Java"
check_command docker "Docker CLI"

if command -v docker >/dev/null 2>&1; then
  if docker info >/dev/null 2>&1; then
    ok "Docker daemon is running."
  else
    error "Docker daemon is not running. Start Docker Desktop or your Docker engine."
  fi
fi

if [ -x "$APP_ROOT/mvnw" ]; then
  ok "Maven wrapper found."
elif [ -x "$APP_ROOT/gradlew" ]; then
  ok "Gradle wrapper found."
else
  error "No Maven or Gradle wrapper found. Generated services should include mvnw or gradlew."
fi

if [ -f "$ENV_FILE" ]; then
  ok ".env file exists."
else
  warn ".env is missing. Copy .env.example to .env or run scripts/setup.sh."
fi

if [ -f "$COMPOSE_FILE" ]; then
  ok "docker-compose.local.yml exists."
else
  error "docker-compose.local.yml is missing."
fi

check_port "${APP_PORT:-8080}" "Application"
check_port "${DB_PORT:-1521}" "Oracle"
check_port "${REDIS_PORT:-6379}" "Redis"

if [ "${BCCS_KAFKA_ENABLED:-true}" != "false" ]; then
  check_port "${KAFKA_PORT:-9092}" "Kafka"
fi

if [ -f "$ENV_FILE" ] && command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  check_compose_service database
  if compose exec -T database healthcheck.sh >/dev/null 2>&1; then
    ok "Oracle is accepting connections."
  else
    warn "Oracle is not accepting connections yet."
  fi

  check_compose_service redis
  if compose exec -T redis redis-cli ping >/dev/null 2>&1; then
    ok "Redis responded to PING."
  else
    warn "Redis is not responding yet."
  fi

  if [ "${BCCS_KAFKA_ENABLED:-true}" != "false" ]; then
    check_compose_service kafka
    if compose exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
      ok "Kafka responded to metadata request."
    else
      warn "Kafka is not responding yet."
    fi
  fi
fi

printf '\nDoctor summary: %s error(s), %s warning(s).\n' "$ERRORS" "$WARNINGS"

if [ "$ERRORS" -gt 0 ]; then
  exit 1
fi

exit 0
