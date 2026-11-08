#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$project_dir"
MAVEN_OPTS="${MAVEN_OPTS:-} -ea" mvn test
