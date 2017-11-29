#!/usr/bin/env bash
set -eu
./gradlew --daemon --parallel clean build test intTest copyToLib 2>&1

printf "Testing application starts up\n"
./startup.sh skip-build

pids=`ps aux | grep java | grep verify-matching-service-adapter.yml | awk '{print $2}'`
for pid in $pids; do
  kill $pid
done

echo "$(tput setaf 1)****Please remember to update RELEASE_NOTES.md ****$(tput sgr0)"
