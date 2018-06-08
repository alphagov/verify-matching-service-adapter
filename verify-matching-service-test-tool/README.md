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

## Configure the Matching Service Test Tool

1. Open the `verify-matching-service-test-tool.yml` file.
2. Replace the `matchUrl` and `accountCreationUrl` URLs with the same URLs you created for your local matching service. Refer to the [guidance on matching requests for more information about matching and account creation URLs](http://alphagov.github.io/rp-onboarding-tech-docs/pages/ms/msBuild.html#respond-to-json-matching-requests).
3. Set `usesUniversalDataset` to `true` or `false` depending on which matching dataset schema [your MSA is configured to use](http://alphagov.github.io/rp-onboarding-tech-docs/pages/matching/matchingserviceadapter.html#in-the-field-europeanidentity).
4. Set `examplesFolderLocation` to the path for the examples directory. If this is not set, the test tool defaults to the `examples/legacy` or `examples/universal-dataset` directory in the application ZIP file, depending on the setting in step 3. If you are using a custom examples directory, it should contain `match` and `no-match` directories with JSON files that you expect to either match or not match.

## Run

To start the test tool, run:
```
bin/verify-matching-service-test-tool verify-matching-service-test-tool.yml
```

This command will run a series of test scenarios as described in [Test scenarios](README.md#Test_scenarios).

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

You can add or amend existing example test datasets or provide a folder containing custom scenarios as described in [Configuration](README.md#configuration).
If you are using the legacy schema, the default scenarios can be found in `examples/legacy` folders.
If you are using the universal schema, these can be found in `examples/universal-dataset`.

For scenarios under the `no-match` folder, the testing tool will expect a no match response from your local matching service.

For scenarios under the `match` folder, the testing tool will expect a match response from your local matching service.

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
