#!/usr/bin/env bash
set -e

cat local.env | sed "s/export //g" | pbcopy
echo "Environment variables copied to clipboard"