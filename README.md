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

gradle is used as a build tool.
There are two sets of tests.

`gradle test`
`gradle intTest`

Deploying
---------

The MSA is distributed as a zip file. We zip the generated jar and all of its dependencies. There is a gradle task for this:

`gradle zip`
