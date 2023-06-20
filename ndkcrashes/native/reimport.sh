#!/usr/bin/env bash

set -e

NATIVE_DIR="$(dirname "$0")"
cd "$NATIVE_DIR"

arc rm -rf crashpad
mkdir -p crashpad
cd crashpad
fetch crashpad
cd crashpad
git checkout "$(cat ../../.commit)"
cd ../..
./update-deps.sh
arc add crashpad
arc reset crashpad
arc co crashpad
