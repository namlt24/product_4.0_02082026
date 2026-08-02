#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 <serviceName> <basePackage> [outputPath]" >&2
  echo "Example: $0 payment-service com.viettel.bccs.payment ../payment-service" >&2
  exit 1
fi

SERVICE_NAME="$1"
BASE_PACKAGE="$2"
OUTPUT_PATH="${3:-../$SERVICE_NAME}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ -e "$OUTPUT_PATH" ]; then
  echo "Error: Output path already exists: $OUTPUT_PATH" >&2
  exit 1
fi

echo "Creating new service $SERVICE_NAME in $OUTPUT_PATH..."

# Copy template to output path, excluding build artifacts and .git
mkdir -p "$OUTPUT_PATH"
# Using rsync if available to exclude, else cp
if command -v rsync >/dev/null 2>&1; then
    rsync -a --exclude 'target' --exclude 'build' --exclude '.git' --exclude '.flattened-pom' --exclude '.flattened-pom.xml' "$TEMPLATE_ROOT/" "$OUTPUT_PATH/"
else
    cp -R "$TEMPLATE_ROOT/"* "$TEMPLATE_ROOT/".* "$OUTPUT_PATH/" 2>/dev/null || true
    rm -rf "$OUTPUT_PATH/target" "$OUTPUT_PATH/build" "$OUTPUT_PATH/.git" "$OUTPUT_PATH/.flattened-pom" "$OUTPUT_PATH/.flattened-pom.xml"
fi

BASE_PACKAGE_PATH="${BASE_PACKAGE//./\/}"
BASE_PACKAGE_PATH="${BASE_PACKAGE_PATH//\\//}"
PACKAGE_NAME="${BASE_PACKAGE}"

SAMPLE_MAIN_ROOT="$OUTPUT_PATH/src/main/java/com/viettel/bccs/template"
SAMPLE_TEST_ROOT="$OUTPUT_PATH/src/test/java/com/viettel/bccs/template"
RENDERED_PACKAGE_ROOT="$OUTPUT_PATH/src/main/java/$BASE_PACKAGE_PATH"
RENDERED_TEST_ROOT="$OUTPUT_PATH/src/test/java/$BASE_PACKAGE_PATH"

if [ -d "$SAMPLE_MAIN_ROOT" ]; then
  mkdir -p "$RENDERED_PACKAGE_ROOT"
  cp -R "$SAMPLE_MAIN_ROOT"/. "$RENDERED_PACKAGE_ROOT"/
  rm -rf "$OUTPUT_PATH/src/main/java/com"
fi

if [ -d "$SAMPLE_TEST_ROOT" ]; then
  mkdir -p "$RENDERED_TEST_ROOT"
  cp -R "$SAMPLE_TEST_ROOT"/. "$RENDERED_TEST_ROOT"/
  rm -rf "$OUTPUT_PATH/src/test/java/com"
fi

# Use find to replace strings in files
find "$OUTPUT_PATH" -type f \( \
  -name '*.java' -o \
  -name '*.md' -o \
  -name '*.xml' -o \
  -name '*.yml' -o \
  -name '*.yaml' -o \
  -name '*.properties' -o \
  -name '*.txt' -o \
  -name '*.ps1' -o \
  -name '*.sh' -o \
  -name '*.gitkeep' \
\) -print0 | while IFS= read -r -d '' file; do
  sed -i.bak \
    -e "s|organization-resource-service|$SERVICE_NAME|g" \
    -e "s|BCCS Service Template|$SERVICE_NAME|g" \
    -e "s|com.viettel.bccs.organization|$PACKAGE_NAME|g" \
    "$file"
  rm -f "$file.bak"
done

echo ""
echo "Service $SERVICE_NAME created successfully at $OUTPUT_PATH!"
echo "Next commands to run:"
echo "  cd $OUTPUT_PATH"
echo "  mvn clean install"
echo "  mvn spring-boot:run"
echo ""
