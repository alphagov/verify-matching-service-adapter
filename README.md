# verify-matching-service-test-tool

An application to help test Verify local matching services
([Source location](https://github.com/alphagov/verify-matching-service-test-tool.git))

### How to use

1. Download the zip [from here](https://github.com/alphagov/verify-matching-service-test-tool/releases)
2. Unzip it
3. Run `bin/verify-matching-service-test-tool`

There are some tests executed as part of the distributed files. However,
additional test cases can be added in `examples/match` and
`examples/no-match` folders within the unzipped folder for match and
no-match cases respectively. Those should be json files (examples can
be found in `examples/match` and `examples/no-match` folders).