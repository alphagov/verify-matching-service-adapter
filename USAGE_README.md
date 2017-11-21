Verify Matching Service Adapter
===============================

The Matching Service Adapter (MSA) is a software tool provided by GOV.UK
Verify. It simplifies communication between your local matching service and the
GOV.UK Verify hub.

For more details, see [the full documentation on how to use the MSA.](http://alphagov.github.io/rp-onboarding-tech-docs/pages/msa/msa.html)

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

