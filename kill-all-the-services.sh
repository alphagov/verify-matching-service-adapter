#!/usr/bin/env bash
. $(dirname $0)/../verify-build-scripts/functions.sh

teardown_services \
        test-rp-msa
