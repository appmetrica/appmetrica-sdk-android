#!/usr/bin/env bash

set -e

NATIVE_DIR="$(dirname "$0")"
cd "$NATIVE_DIR/crashpad/crashpad"

function fix() {
  local type="$1"
  local name="$2"

  local regex="^\(.*\)$type == \"$name\"\(.*\)$"
  local newStr="\1\"$name\" == \"$name\"\2"
  local newStrWithComment="#region change for AppMetrica\n$newStr\n#endregion change for AppMetrica"
  echo "sed 's/$regex/$newStrWithComment/g'"
  sed -i.bak -e "s/$regex/$newStrWithComment/g" DEPS && rm DEPS.bak || exit 1
}

function removeDuplicateComments() {
    sed -i.bak '$!N; /^\(.* change for AppMetrica\)\n\1$/!P; D' DEPS && rm DEPS.bak || exit 1
}

# need download binary files for all os
fix "host_os" "mac"
fix "host_os" "linux"
fix "host_cpu" "arm64"
fix "host_cpu" "x64"
removeDuplicateComments
gclient sync
