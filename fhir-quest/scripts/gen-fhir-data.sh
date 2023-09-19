#!/usr/bin/env bash

TMP_DIR="$1"
SYNTHEA_ARGS="${@:2}"
SYNTHEA_JAR="$TMP_DIR/synthea.jar"

if ! which java >> /dev/null; then
  echo "Error: cannot find a Java runtime..."
  exit 1
fi

mkdir -p "$TMP_DIR"
if ! [[ -f "$SYNTHEA_JAR" ]]; then
  echo "fetching Synthea jar..."
  curl --progress-bar -SLo "$SYNTHEA_JAR" \
    "https://github.com/synthetichealth/synthea/releases/download/master-branch-latest/synthea-with-dependencies.jar"
fi

java -jar "$SYNTHEA_JAR" \
  --exporter.baseDirectory "$TMP_DIR" \
  --exporter.fhir.us_core_version 4.0.1 \
  --exporter.years_of_history 10 \
  -a 0-75 \
  -p 1000 \
  $SYNTHEA_ARGS
