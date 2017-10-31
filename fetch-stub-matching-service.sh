#!/usr/bin/env bash

# This script is intended to be run on Jenkins to download the latest version of the stub local
# matching service from https://github.com/alphagov/verify-stub-matching-service/releases

set -e
set -u

download_url=$(
  curl --fail "https://api.github.com/repos/alphagov/verify-stub-matching-service/releases/latest" |
  ruby -e 'require "json"; puts JSON.parse(ARGF.read).fetch("assets").fetch(0).fetch("browser_download_url")'
)

curl -o "local-matching-service.jar" -L "$download_url"

