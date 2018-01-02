# Verify Matching Service Test Tool

A [local matching service](http://alphagov.github.io/rp-onboarding-tech-docs/pages/ms/msWorks.html) allows you to find a match between a user’s verified identity and a record in your organisation's database(s).

The Verify Matching Service Test Tool helps you check your local matching service can:
* find and match records correctly
* identify unmatched records correctly

You can use the test tool while [building your local matching service](http://alphagov.github.io/rp-onboarding-tech-docs/pages/ms/msBuild.html) and include it any automated testing. 

## Prerequisites

* Java 8

## Installation

Unzip Test Tool zip file

## Configuration

Open the `verify-matching-service-test-tool.yml` file.

Replace the `matchUrl` and `accountCreationUrl` URLs with the same URLs you created for your local matching service. Refer to the [guidance on matching requests for more information about matching and account creation URLs](http://alphagov.github.io/rp-onboarding-tech-docs/pages/ms/msBuild.html#respond-to-json-matching-requests).

## Run

Run `bin/verify-matching-service-test-tool` to start the test tool.

This command will run a series of test scenarios.

## Optional Command Line Arguments

You can also use command line arguments to specify the relative location of the configuration file and/or the relative location of the directory containing the additional scenario files described below.

e.g. `bin/verify-matching-service-test-tool -c <../config-file.yml> -e <../examplesFolder>`

NB: the 'examplesFolder' should be the relative path of the folder containing the 'match' and 'no-match' sub-folders.

`bin/verify-matching-service-test-tool --help` will display the available command line arguments.

## Test scenarios

The tool will run 6 default test scenarios:

* Simple request with level of assurance 1 
* Complex request with level of assurance 1
* Simple request with level of assurance 2
* Complex request with level of assurance 2
* Simple request with address missing optional fields
* Simple user account creation request

### Adding additional scenarios

You can add or amend existing test scenarios in the `examples/match` and
`examples/no-match` folders.

Add any additional test scenarios as new JSON files within these folders.

For example, to match the identity of a user with the first name `Sam`, you could amend the `example-match.json` as below:

```diff
"firstName": {
- "value": "Joe",
+ "value": "Sam",
  "verified": true
},
```

## Support and feedback

For non-security related bugs and feature requests please raise an issue in the [GitHub issue tracker](https://github.com/alphagov/verify-matching-service-test-tool/issues).

If you think you have discovered a security issue in this code please email disclosure@digital.cabinet-office.gov.uk with details.

## Licence

[MIT](/LICENSE)
