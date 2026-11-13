#!/usr/bin/env bash
# Run the Maestro suite (or one flow) against the first connected Android device.
# Usage: scripts/run-maestro.sh [flow-name-without-extension] [extra maestro args...]
set -euo pipefail
cd "$(dirname "$0")/.."

if pgrep -f "Maestro Studio.app" >/dev/null 2>&1; then
  echo "Maestro Studio is running and holds the device session. Quit it first." >&2
  exit 1
fi

ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
if ! "$ADB" shell pm list packages 2>/dev/null | grep -q com.example.stackdemo; then
  echo "App not installed, running ./gradlew installDebug"
  ./gradlew installDebug -q
fi

target=".maestro/"
if [[ $# -gt 0 && -f ".maestro/$1.yaml" ]]; then
  target=".maestro/$1.yaml"; shift
fi
exec maestro test "$@" "$target"
