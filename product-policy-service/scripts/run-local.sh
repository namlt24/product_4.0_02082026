#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$APP_ROOT/.env"

error() { printf '[ERROR] %s\n' "$*" >&2; }

if [ ! -f "$ENV_FILE" ]; then
  error "Missing .env. Run scripts/setup.sh first or copy .env.example to .env."
  exit 1
fi

set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a

cd "$APP_ROOT"

if [ -x "$APP_ROOT/mvnw" ]; then
  exec "$APP_ROOT/mvnw" spring-boot:run -Dspring-boot.run.profiles="${SPRING_PROFILES_ACTIVE:-local}"
fi

if [ -x "$APP_ROOT/gradlew" ]; then
  exec "$APP_ROOT/gradlew" bootRun --args="--spring.profiles.active=${SPRING_PROFILES_ACTIVE:-local}"
fi

error "No Maven or Gradle wrapper found. Add mvnw or gradlew to the generated service."
exit 1
