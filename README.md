# Verify Matching Service Adapter

>**GOV.UK Verify has closed**
>
>This repository is out of date and has been archived

The Verify Matching Service Adapter (MSA) is component you can use to handle communication with the Verify hub. The MSA converts the SAML message received from the Hub into JSON data that is then passed onto a Local Matching Service and vice versa. It also handles the encryption, decryption, and signing of the SAML messages. It is a stateless Web Service that runs on Java 8 and was built using the Dropwizard framework and Gradle build tool.

## How does it work?

The MSA works by receiving a SAML message containing an AttributeQuery (XML formatted message sent via SOAP) from the hub asking "do you know who this person is?", the MSA unpacks the Attributes from the XML and translates them into a "matching dataset" in JSON.  The MSA then posts this JSON to a Local Matching Service which is to be built by the service team.  When the MSA unpacks the SAML it validates XML signatures using x509 public certificates which are distributed through metadata.

## Run (in development)

To run the matching service adapter locally, generate a `local.env` file either using [verify-local-startup](https://github.com/alphagov/verify-local-startup) or `openssl` and `keytool`.

To run from terminal, run `./startup.sh`.

To run in IntelliJ, create a run configuration with the following properties:
```
Main class: uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication
Program arguments: server configuration/verify-matching-service-adapter.yml
Use classpath of module: verify-matching-service-adapter_main
```

The environment variables from `local.env` can be added to this configuration by running `env-for-intellij.sh` to copy them to the clipboard before pasting them into the environment variable window in the run configuration.

## Test

The MSA includes both unit and integration tests that can be run with Gradle.

`./gradlew test`

`./gradlew intTest`

## Generate the artefact

The MSA is distributed as a zip file containing the generated jar and all of its dependencies.

Run the following to create the zip.

`./gradlew zip`


Verify Matching Service Test Tool
---------------------------------
The MSA is distributed as a zip file which also contains the [Verify Matching Service Test tool](https://github.com/alphagov/verify-matching-service-adapter/tree/master/verify-matching-service-test-tool).

The test tool helps you check your matching service can:

* handle matching datasets
* find and match records correctly
* handle matching failures

## Support and responsible disclosure

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-matching-service-adapter/issues/new) in the GitHub issue tracker.

## License

[MIT](https://github.com/alphagov/verify-matching-service-adapter/blob/master/LICENCE)
