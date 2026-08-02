#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

error() { printf '[ERROR] %s\n' "$*" >&2; }
info() { printf '[INFO] %s\n' "$*"; }

cd "$APP_ROOT"

if [ -x "$APP_ROOT/mvnw" ]; then
  info "Running Maven unit and integration tests with wrapper."
  exec "$APP_ROOT/mvnw" verify
fi

if [ -x "$APP_ROOT/gradlew" ]; then
  info "Running Gradle unit and integration tests with wrapper."
  if "$APP_ROOT/gradlew" tasks --all | grep -q '^integrationTest'; then
    exec "$APP_ROOT/gradlew" test integrationTest
  fi
  exec "$APP_ROOT/gradlew" test
fi

error "No Maven or Gradle wrapper found. Add mvnw or gradlew to the generated service."
exit 1
