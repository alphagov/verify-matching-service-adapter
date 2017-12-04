Matching Service Adapter
========================

What is MSA?
------------

The MSA receives a SOAP formatted matching request from the hub and sends a JSON POST to a matching service hosted by an RP.

How does it work?
-----------------

The MSA works by receiving a signed XML SAML-SOAP AttributeQuery from the hub asking "do you know who this person is?", the MSA unpacks the "matching dataset" from the XML and makes a JSON POST request to a Local Matching Service which has to be built by the service team. When the MSA does the unpacking it validates XML signatures using x509 public certificates which are distributed through metadata.

Testing
-------

`gradle` is used as a build tool.
There are two sets of tests.

`./gradlew test`

`./gradlew intTest`

Deploying
---------

The MSA is distributed as a zip file. We zip the generated jar and all of its dependencies. There is a gradle task for this:

`./gradlew zip`

Running locally
---------------
To run the matching service adapter locally, generate a `local.env` file either using [verify-local-startup](https://github.com/alphagov/verify-local-startup) or `openssl` and `keytool`

To run from terminal, run `./startup.sh`

To run in IntelliJ, create a run configuration with the following properties:
```
Main class: uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication
Program arguments: server configuration/verify-matching-service-adapter.yml
Use classpath of module: verify-matching-service-adapter_main
```

The environment variables from `local.env` can be added to this configuration by running `env-for-intellij.sh` to copy them to the clipboard before pasting them into the environment variable window in the run configuration

Verify Matching Service Test Tool
---------------------------------

The MSA is distributed as a zip file which also contain the Verify Matching Service Test tool.

The Verify Matching Service Test Tool is designed to help you develop your Local Matching Service.

Detailed information on how to use Verify Matching Service Test Tool is provided [here](https://github.com/alphagov/verify-matching-service-adapter/verify-matching-service-test-tool).

Support and raising issues
--------------------------

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-matching-service-adapter/issues/new) in the GitHub issue tracker.