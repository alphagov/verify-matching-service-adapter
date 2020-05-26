Verify Matching Service Adapter
===============================

The Matching Service Adapter (MSA) is a software tool provided by GOV.UK
Verify. It simplifies communication between your local matching service and the
GOV.UK Verify hub.

It is published as a zip file that contains the jar file and all of its dependencies.
See the [release notes](RELEASE_NOTES.md) for specific release information.

For more details, see [the full documentation on how to use the MSA.](https://www.docs.verify.service.gov.uk/legacy/build-ms/msa/#matching-service-adapter)

Pre-requisites
--------------

To install and run MSA, you need:

* Java Runtime Environment (JRE) version 8
* 512 MB to 1 GB of RAM (on top of what you need to run your operating system)

Running the MSA
---------------

To start the MSA, run one of the following commands:

```
# With config for a test environment (e.g. with compliance tool):
java -jar verify-matching-service-adapter.jar server test-config.yml
```

```
# With config for the production environment:
java -jar verify-matching-service-adapter.jar server prod-config.yml
```

Using the stub local matching service
-------------------------------------

If you haven't built your local matching service yet but want to test that the matching service
adapter is working correctly you can use the provided local-matching-service.jar file. This is a
stub implementation of a local matching service which will always return `{"result":"match"}`.

You can run the stub local matching service with:

```
# Start the service on the default port (50130)
java -jar stubs/local-matching-service.jar
```

If you want to start the stub local matching service on a different port you can set a `LMS_PORT`
environment variable. For example:

```
# In bash (or similar)
export LMS_PORT=1337
java -jar stubs/local-matching-service.jar

# In powershell
$env:LMS_PORT=1337
java -jar stubs/local-matching-service.jar
```

