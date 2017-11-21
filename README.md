# Verify Matching Service Adapter

The Verify Matching Service Adapter (MSA) is component you can use to handle communication with the Verify hub. The MSA converts SAML into JSON and vice versa. It also handles the encryption, decryption, and signing of SAML messages. It is a stateless service and runs on Java 8 using Dropwizard.

:question: These statements are from the existing onboarding guide - are they still accurate?

If you are a government service connecting to GOV.UK Verify, refer to the USAGE_README for more information on [how to install and use the MSA](USAGE_README.md).

## Pre-requisites

To install and run MSA, you need:

* Java Runtime Environment (JRE) version 8
* 512 MB to 1 GB of RAM (on top of what you need to run your operating system)

## How does it work?

The MSA works by receiving a signed XML SAML-SOAP AttributeQuery from the hub asking "do you know who this person is?", the MSA unpacks the "matching dataset" from the XML and makes a JSON POST request to a Local Matching Service which has to be built by the service team. When the MSA does the unpacking it validates XML signatures using x509 public certificates which are distributed through metadata.

## Install
The MSA uses Gradle as a build tool and is distributed as a zip file containing the generated jar and all of its dependencies. See the [release notes](RELEASE_NOTES.md) for specific release information.

Run the following to download the zip.

`./gradlew zip`

## Run

:question: There seems to be a conflicting set of instructions to run the thing in this README and the USAGE_README. Is the below only relevant for internal folks?

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

The MSA provides two tests with Gradle.

`./gradlew test`

`./gradlew intTest`

:question: What is the different between these tests? What do they test for and what do they return?

## Support and responsible disclosure

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-matching-service-adapter/issues/new) in the GitHub issue tracker.

## License

[MIT](https://github.com/alphagov/verify-matching-service-adapter/blob/master/LICENCE)
