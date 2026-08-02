#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 4 ]; then
  echo "Usage: render-template.sh <serviceName> <basePackage> <servicePackage> <outputPath>" >&2
  exit 1
fi

SERVICE_NAME="$1"
BASE_PACKAGE="$2"
SERVICE_PACKAGE="$3"
OUTPUT_PATH="$4"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ -e "$OUTPUT_PATH" ]; then
  echo "Output path already exists: $OUTPUT_PATH" >&2
  exit 1
fi

cp -R "$TEMPLATE_ROOT" "$OUTPUT_PATH"
rm -rf "$OUTPUT_PATH/target" "$OUTPUT_PATH/.flattened-pom" "$OUTPUT_PATH/.flattened-pom.xml"

BASE_PACKAGE_PATH="${BASE_PACKAGE//./\/}"
BASE_PACKAGE_PATH="${BASE_PACKAGE_PATH//\\//}"
SERVICE_PACKAGE_PATH="${SERVICE_PACKAGE//./\/}"
SERVICE_PACKAGE_PATH="${SERVICE_PACKAGE_PATH//\\//}"
PACKAGE_NAME="${BASE_PACKAGE//\//.}.${SERVICE_PACKAGE//\//.}"
APPLICATION_CLASS_NAME="$(printf '%s' "$SERVICE_NAME" | awk -F'[^A-Za-z0-9]+' '{
  for (i = 1; i <= NF; i++) {
    if ($i != "") {
      printf toupper(substr($i, 1, 1)) substr($i, 2)
    }
  }
}')Application"
PLACEHOLDER_PACKAGE_ROOT="$OUTPUT_PATH/src/main/java/com.viettel.bccs/productcatalog"
RENDERED_PACKAGE_ROOT="$OUTPUT_PATH/src/main/java/$BASE_PACKAGE_PATH/$SERVICE_PACKAGE_PATH"
SAMPLE_MAIN_ROOT="$OUTPUT_PATH/src/main/java/com/viettel/bccs/template"
SAMPLE_TEST_ROOT="$OUTPUT_PATH/src/test/java/com/viettel/bccs/template"
RENDERED_TEST_ROOT="$OUTPUT_PATH/src/test/java/$BASE_PACKAGE_PATH/$SERVICE_PACKAGE_PATH"

if [ -d "$PLACEHOLDER_PACKAGE_ROOT" ]; then
  mkdir -p "$(dirname "$RENDERED_PACKAGE_ROOT")"
  mv "$PLACEHOLDER_PACKAGE_ROOT" "$RENDERED_PACKAGE_ROOT"
  rm -rf "$OUTPUT_PATH/src/main/java/com.viettel.bccs"
fi

if [ -d "$SAMPLE_MAIN_ROOT" ]; then
  mkdir -p "$RENDERED_PACKAGE_ROOT"
  cp -R "$SAMPLE_MAIN_ROOT"/. "$RENDERED_PACKAGE_ROOT"/
  rm -rf "$SAMPLE_MAIN_ROOT"
fi

if [ -d "$SAMPLE_TEST_ROOT" ]; then
  mkdir -p "$RENDERED_TEST_ROOT"
  cp -R "$SAMPLE_TEST_ROOT"/. "$RENDERED_TEST_ROOT"/
  rm -rf "$SAMPLE_TEST_ROOT"
fi

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
    -e "s|product-catalog-service|$SERVICE_NAME|g" \
    -e "s|com.viettel.bccs|$BASE_PACKAGE|g" \
    -e "s|productcatalog|$SERVICE_PACKAGE|g" \
    -e "s|product-catalog-service|$SERVICE_NAME|g" \
    -e "s|com.viettel.bccs.productcatalog|$PACKAGE_NAME|g" \
    -e "s|ProductCatalogServiceApplication|$APPLICATION_CLASS_NAME|g" \
    -e "s|ProductCatalogServiceApplication|$APPLICATION_CLASS_NAME|g" \
    "$file"
  rm -f "$file.bak"
done
for template_application_file in \
  "$RENDERED_PACKAGE_ROOT/ProductCatalogServiceApplication.java" \
  "$RENDERED_PACKAGE_ROOT/ProductCatalogServiceApplication.java"; do
  if [ -f "$template_application_file" ]; then
    mv "$template_application_file" "$RENDERED_PACKAGE_ROOT/$APPLICATION_CLASS_NAME.java"
    break
  fi
done
echo 'Rendered BCCS service template to '$OUTPUT_PATH
