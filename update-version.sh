#!/bin/bash


set -e

./gradlew updateVersion -Pversion="$1"
./aarDump.sh
