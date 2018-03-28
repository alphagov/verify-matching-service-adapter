
#!/usr/bin/env bash

# Use the local environment if one exists
if test -e local.env; then
    set -a
    source local.env
    set +a
    port=$TEST_RP_MSA_PORT
else
    printf "$(tput setaf 1)No local environment found. Use verify-local-startup or openssl to generate a local.env file\n$(tput sgr0)"
    exit
fi

# Kill existing instances
pkill -9 -f msa

mkdir -p logs
if test ! "$1" == "skip-build"; then
    ./gradlew clean
    ./gradlew build copyToLib
fi

printf "Starting... "
./gradlew run > logs/verify-matching-service-adapter_console.log 2>&1 &
if test -d ../verify-local-startup; then
    source ../verify-local-startup/lib/services.sh
    ( start_service_checker "matching-service-adapter" $port "Unknown" "logs/verify-matching-service-adapter_console.log" "localhost:$port/service-status"
        wait )
else
    sleep 10
    curl --silent --output /dev/null "localhost:$port/service-status" && printf "$(tput setaf 2)STARTED$(tput sgr0) [port: $port]\n" && exit
    printf "$(tput setaf 1)\nProcess started but may not be alive - check with \`jps\` or \`curl localhost:$TEST_RP_MSA_PORT/service-status\`\n$(tput sgr0)"
fi
