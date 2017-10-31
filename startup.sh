#!/usr/bin/env bash

# Use the local environment if one exists
if test -e local.env; then
    source local.env
    port=$TEST_RP_MSA_PORT
else
    port=50210
fi

# Kill existing instances
pkill -9 -f msa

mkdir -p logs
if test ! "$1" == "skip-build"; then
    ./gradlew clean build copyToLib
fi

printf "Starting... "
./gradlew run > logs/verify-matching-service-adapter_console.log 2>&1 &
if test -d ../verify-local-startup; then
    source ../verify-local-startup/lib/services.sh
    ( start_service_checker $port "Unknown" "logs/verify-matching-service-adapter_console.log"
        wait )
else
    sleep 10
    curl --silent --output /dev/null "localhost:$port/service-status" && printf "$(tput setaf 2)STARTED$(tput sgr0) [port: $port]\n" && exit
    printf "$(tput setaf 1)\nProcess started but may not be alive - check with \`jps\` or \`curl localhost:$TEST_RP_MSA_PORT/service-status\`\n$(tput sgr0)"
fi
