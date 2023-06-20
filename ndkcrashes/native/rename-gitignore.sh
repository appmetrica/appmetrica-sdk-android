#!/usr/bin/env bash

set -e

# rename .gitignore for opensource
find crashpad -name .gitignore -exec sh -c 'f="{}"; mv "${f}" "${f}.bak"' \;
