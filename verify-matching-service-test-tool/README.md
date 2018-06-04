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

The tool offers scenarios for two JSON schemas:

* **legacy matching dataset**: you will use this if your service does not support European identities and you have not enabled this functionality in your version of the Matching Service Adapter (MSA)
* **universal matching dataset**: you will use this if your service supports European identities and you have enabled this functionality in your version of the MSA. The universal matching dataset supports assertions from both GOV.UK Verify identity providers (IdP) and identities from other European countries.

If your local matching service supports the universal matching dataset, set `usesUniversalDataset` to true.


## Run

Run `bin/verify-matching-service-test-tool` to start the test tool.

This command will run a series of test scenarios.

## Optional Command Line Arguments

You can also use command line arguments to specify the relative location of the configuration file and/or the relative location of the directory containing the additional scenario files described below.

e.g. `bin/verify-matching-service-test-tool -c <../config-file.yml> -e <../examplesFolder>`

NB: the 'examplesFolder' should be the relative path of the folder containing the 'match' and 'no-match' sub-folders.

`bin/verify-matching-service-test-tool --help` will display the available command line arguments.

## Test scenarios

The tool will run the following scenarios for the legacy matching dataset (if `usesUniversalDataset` is set to false):

* LoA1 - Minimum data set (first name, surname, DOB, a single address) 
* LoA2 - Minimum data set (first name, surname, DOB, a single address) 
* LoA1 - Extended data set (historical names, gender, historical addresses, some unverified, with at least one verified address) 
* LoA2 - Extended data set (historical names, gender, historical addresses, some unverified, with at least one verified address) 
* User account creation functionality

The following scenarios using the universal matching dataset will allow you to test your matching service against assertions from a Verify IdP:

* LoA1 - Minimum data set (first name, surname, DOB, a single address) 
* LoA2 - Minimum data set (first name, surname, DOB, a single address) 
* LoA1 - Extended data set (historical names, gender, addresses, some unverified) 
* LoA2 - Extended data set (historical names, gender, addresses, some unverified) 
* User account creation functionality

The following scenarios allow you to test your matching service against assertions from other European countries:

* eIDAS - LoA2 - Standard data set (first name, surname, DOB) 
* eIDAS - LoA2 - Standard data set - special characters in name fields (first name with special characters, surname with special characters, DOB) 
* eIDAS - LoA2 - Standard data set - transliteration provided for name fields (first name in original script, first name in latin script, surname in original script, surname in latin script, DOB)

### Adding additional scenarios

You can add or amend existing example test datasets.
If you are running in legacy mode, these can be found in `examples/legacy/match` and`examples/legacy/no-match` folders.
If you are running in universal matching dataset mode, these can be found in `examples/universal-dataset/match` and `examples/universal-dataset/no-match`.

For scenarios under the corresponding `no-match` folder, the testing tool will expect a no match response from your local matching service.

For scenarios under the corresponding `match` folder, the testing tool will expect a match response from your local matching service.

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
