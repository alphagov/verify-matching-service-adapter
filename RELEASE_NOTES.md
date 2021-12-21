MSA Release Notes
=================

### Next

### 5.1.1
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/5.1.0...5.1.1)
* Use saml-libs release that removes eIDAS code
* Use Maven central for dependencies.
* Update logback library to version 1.2.9

### 5.1.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/5.0.0...5.1.0)
* [Removes matching from european identity schemes](doc/release-details/5.1.0.md), as the UK is longer part of the eIDAS regulation. If the configuration `europeanIdentity.enabled` is currently set to `true`, it must not be removed or changed.

### 5.0.2
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/4.2.1...5.0.0)
* Breaking change for configuration. Default metadata trust stores are now used by default and no longer need configuring. The defaults can still be overridden. If you are not using custom trust stores you will need to remove the config to allow the defaults to work. In particular `test_ida_metadata.ts` and `prod_ida_metadata.ts` no longer exist. See below for config change details.
* Breaking change for configuration. The `europeanIdentity.aggregatedMetadata` configuration now no longer requires values for `trustStore`, `trustAnchorUri` and `metadataSourceUri`. Default values are used instead when `europeanIdentity.aggregatedMetadata.environment` is specified as `PRODUCTION` or `INTEGRATION`. The default values can be overridden if desired. As above, `test_ida_metadata.ts` and `prod_ida_metadata.ts` no longer exist. See below for config change details.
* Add new G3 CAs to the production and test trust stores, as well as new dev-pki CAs to the test trust stores.
* Various updates to allow the backwards compatibility tests to run.
* UKCloud artifactory has been replaced with new JFrog SaaS artifactory.

#### Configuration change details
```diff
 metadata:
   environment: INTEGRATION
-  trustStore:
-    path: test_ida_metadata.ts
-    password: puppet

 europeanIdentity:
   enabled: true
   aggregatedMetadata:
     jerseyClientName: trust-anchor-client
+    # Choices are (INTEGRATION|PRODUCTION)
+    environment: INTEGRATION
-    trustAnchorUri: https://www.integration.signin.service.gov.uk/SAML2/metadata/trust-anchor
-    metadataSourceUri: https://www.integration.signin.service.gov.uk/SAML2/metadata/aggregator
-    trustStore:
-      path: test_ida_metadata.ts
-      password: puppet
```

### 4.2.1
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/4.1.0...4.2.1)

* Handle unsigned eIDAS assertions in a secure manner. At the moment, unsigned assertions are not allowed. The eIDAS specification does not require signed assertions, whereas the Verify specification does. This release adds logic to accept unsigned eIDAS assertions. Signed eIDAS and Verify assertions are still verified as previously. This change will allow the MSA to accept eIDs from more European countries.
* Configuration parameter `hubConnectorEntityId` in the `europeanIdentity` section is now optional and can be removed from the configuration file.

### 4.1.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/4.0.0...4.1.0)

* Revert changes to allow unsigned eIDAS assertions. An issue was identified and a new approach will be released in a future version.
* Upgraded `verify-saml-libs` to build 209
* Upgraded `opensaml` to version 3.4.3

### 4.0.1
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/4.0.0...4.0.1)

* Upgraded `verify-saml-libs` to build 206
* Upgraded `ida_utils` to build 370
* Removed dependency on `trust-anchor-cli` which brought in a security vulnerability through the `wiremock-standalone-2.16.0.jar` library. This library contains a jar file `handlebars-v4.0.4.js` which has a known security vulnerability.

### 4.0.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/3.1.0...4.0.0)

* Allow `OneTimeUse` validation with eIDAS assertions. The MSA doesn't allow the use of the `OneTimeUse` attribute with assertions from IDPs, however there are some eIDAS countries that use it. The MSA will now accept eIDAS assertions with zero or one `OneTimeUse` elements.
* Allow unsigned eIDAS assertions. The eIDAS specification does not require signed assertions, whereas the Verify specification does. Signed eIDAS assertions are still verified.
* Added additional detail and stack trace to error responses. This is enabled by default but can be
disabled by setting `returnStackTraceInErrorResponse: false` in configuration file.
* If multiple firstnames are provided by IDP, the MSA will select which one to pass to local matching service based
on the following rules:
    1. the verified current firstname
    1. if no verified firstname, then the first current is selected
    1. if no verified or current then the first provided by the IDP
* When calling user account creation, if multiple current values exist for any attribute, the first verified value will be preferred over non-verified values.
* Removed redundant header `ida-msa-version` from matching response.
* Reduced Jersey clients `keepAlive` to 10 seconds. This is to prevent `NoHttpResponseException`'s being thrown due to servers having an `idleTimeout` of 30 seconds.
* `environment` is now a required variable in the metadata section of the [MSA configuration](https://github.com/alphagov/rp-onboarding-tech-docs/blob/master/source/pages/matching/matchingserviceadapter.rst#in-the-field-metadata). This previously defaulted to `INTEGRATION` allowing it to be easily set incorrectly.
* Upgraded `verify-saml-libs` to build 192
* Upgraded Jackson databind libs to 2.9.7 to address security vulnerabilities.
* Upgraded OpenSAML to 3.4.0
* Upgraded Mockito and Jacoco to ensure the MSA runs with Java 11
* Upgraded Gradle to 5.0
* Removed local debug option

### 3.1.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/3.0.2...3.1.0)

* Upgraded SAML libs version so that the MSA can support the ECDSA and RSASSA-PSS signing algorithms to fulfil the eIDAS cryptographic requirements as laid out in the ["eIDAS - Cryptographic requirements for the Interoperability Framework"](https://ec.europa.eu/cefdigital/wiki/download/attachments/46992719/eidas_-_crypto_requirements_for_the_eidas_interoperability_framework_v1.0.pdf?version=1&modificationDate=1497252920224&api=v2). 
* Upgraded to Dropwizard 1.3.5.
* Extended the capability of the GOV.UK Verify Hub MSA health-check to return information on whether you:
  * enabled `europeanIdentity` in your [MSA configuration](http://alphagov.github.io/rp-onboarding-tech-docs/pages/matching/matchingserviceadapter.html#in-the-field-europeanidentity)
  * are using SHA-256. By default, the MSA signs messages using SHA-256. To keep this setting, make sure `shouldSignWithSHA1` is set to `false` in your [MSA configuration](http://alphagov.github.io/rp-onboarding-tech-docs/pages/matching/matchingserviceadapter.html#configure-the-matching-service-adapter).
 
### 3.0.3
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/3.0.2...3.0.3)

* Revoked SAML message changes introduced in [release 3.0.3](RELEASE_NOTES.md#303). The set of IDP assertion validations is now the same as before release 3.0.0.


### 3.0.2
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/3.0.1...3.0.2)

* **Contains the change revoked in release 3.0.3. Please use release 3.0.3 or the latest release available.**
* Re-introduced post-matching logging status which had been removed in [release 3.0.0](RELEASE_NOTES.md#300)
* Included support for compilation on Java 9 and 10. Compilation on Java 9 or 10 will produce a multi-release JAR that will run on Java 8, 9, and 10. This release was compiled on Java 8 and does not contain a multi-release JAR.


### 3.0.1
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/3.0.0...3.0.1)

* **Contains the change revoked in release 3.0.3. Please use release 3.0.3 or the latest release available.**
* Minor update of documentation


### 3.0.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/2.2.0...3.0.0)

* Added validations to `Conditions` within the `Assertion` element of the SAML message. **This change was revoked in [release 3.0.3](RELEASE_NOTES.md#303). Please use release 3.0.3 or the latest release available.**
* [Enabled support for accepting identities from other European countries](doc/release-details/3.0.0.md#support-for-european-identities) as part of eIDAS compliance.
* [Added a configuration option to the Matching Service Test Tool](README.md#configuration) to allow testing using the new [universal JSON matching schema](http://alphagov.github.io/rp-onboarding-tech-docs/pages/matching/buildmatchingservice.html#jsonschema).
* Simplified Matching Service Test Tool to use only a YAML configuration file.
* [Upgraded to SHA-256 as the default secure hashing algorithm](doc/release-details/3.0.0.md#upgrade-to-sha-256) for signing and encrypting SAML messages.
* Updated the test IDAP root Certificate Authority.
* Separated the GOV.UK Verify Hub and IDP truststores to improve security.
* Included the Hub and IDP truststore files in the JAR archive to simplify the MSA deployment process.
* Upgraded to using Dropwizard version 1.3.2.

#### Required configuration changes
* Remove the the `hub.trustStore` section and instead set `environment` in the `metadata` section:
```diff
 hub:
   ssoUrl: https://www.integration.signin.service.gov.uk/SAML2/SSO
-  trustStore:
-    path: test_ida_hub.ts
-    password: puppet

 metadata:
+  # Choices are (INTEGRATION|PRODUCTION)
+  environment: INTEGRATION
  trustStore:
    path: test_ida_metadata.ts
    password: puppet
```


### 2.2.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/2.1.0...2.2.0)

* Open sourced the verify-matching-service-adapter :tada:
* Made the checks we perform on assertions from identity providers more strict
* Improved support for configuration via environment variables
* Added experimental support for running inside Docker
* Improved support for semantic version numbers
* Added verify-matching-service-test-tool to the open source repository (see https://github.com/alphagov/verify-matching-service-adapter/tree/master/verify-matching-service-test-tool)
* Partially implemented support for use with european identity schemes through eIDAS (disabled by default).


### 2.1.0
[View Diff](https://github.com/alphagov/verify-matching-service-adapter/compare/2.1.0...2.1.0)

11th September 2017 - v2.1.0-592
* No longer allow MD5 digest and signature algorithms
* Add address history as a user account creation attribute
 - Add ADDRESS_HISTORY to the UserAccountCreationAttribute enum and support for it in UserAccountCreationAttributeExtractor
 - If a transaction is configured with ADDRESS_HISTORY in its list of user account creation attributes, then the MSA will extract the user's full address history and return it to the hub as an "addresshistory" SAML attribute.
 - Address history will be added to the onboarding form at a later date for Relying Parties to request it.

### 2.0.0

16th May 2017 - v2.0.0-580

* new semantic version number for the MSA - starting at v2.0.0. The number after the - represent the build number. This is the version number we used to use for the MSA
* Note that Hashed Pids received by the Local Matching Service, and Relying Party service, will be different for the same user from the same IDP in the case that they return with a different level of assurance.  This is similar in effect to the same user using multiple IDPs, but also taking into account the level of assurance.
* gzip has been set to disabled by default for HTTP requests from the MSA to the Local Matching Service.
  If you prefer requests to be compressed, you can set the following in you -config.yml files:
  ```
  localMatchingService:
    client:
      gzipEnabledForRequests: true
  ```
* the Verify Hub's entityId is now fully configurable in the MSA, and has an appropriate default value - <em>users do not need to configure this in the yaml during normal operation</em>.  To configure a different entityId for a hub set both `hub.hubEntityId` and `metadata.hubEntityId` in the yaml
* Added instructions and documentation for a stub Local Matching Service for use when onboarding
* miscellaneous library upgrades, big fixes and improvements

Feb 22 2017 -- v.561
* Worked around a bug in Shibboleth SP preventing it from consuming MSA metadata.
* Fixed the format of the release .zip file to work on windows

Feb 7 2017 -- v.555
* Simplified application configuration
 - Set defaults for many config options
 - Added localMatchingService section to group local matching service related config
   - matchingServiceUri -> matchUrl
   - unknownUserCreationServiceUri -> accountCreationUrl
   - matchingServiceClient -> client
 - Added matchingServiceAdapterSection to group MSA related config and renamed the following keys
   - matchingServiceAdapterLocation -> externalUrl
   - entityId has now moved under matchingServiceAdapter
 - Added hub section to group hub related config
   - requireHubCertificates -> republishHubCertificatesInLocalMetadataa
 - Renamed the following keys under created truststore section under metadata
   - trustStorePath -> trustStore
   - trustStorePassword -> password
 - Renamed publicKey keyUri -> certFile
 - Renamed privateKey keyUri -> keyFile
 - Renamed keyName -> name
 - Renamed storeUri -> path
* The zip file now contains minimal configuration files for both test (test-config.yaml) and production (prod-config.yaml) and should work 'out of the box' with these files
* Example config files contain comments to help guide the configuration of the MSA

Jan 10 2017 -- v.547
* Moved CertificateStore to new package to prevent it clashing with a class in a dependency

December 9 2016 -- v. 538
* Added Open Government Licence v3
* Fixed bug for configuring http proxy settings via yaml file.

November 16, 2016 -- v. 534
* SAML libraries upgraded to OpenSAML3
* Added config property 'publicSecondaryEncryptionKeyConfiguration'. If the RP is currently using secondary keys they will need to add the corresponding keyUri and keyName to the config

* Metadata is now retrieved by the MSA from the Verify hub's signed 'federation' metadata instead of the old 'sp' metadata
 - metadataUri is removed configuration.
 - Add metadata-block to configuration. See the official documentation for details: http://alphagov.github.io/rp-onboarding-tech-docs/pages/msa/msaUse.html
    for production the url is: https://www.signin.service.gov.uk/SAML2/metadata/federation
    for integration the url is: https://www.integration.signin.service.gov.uk/SAML2/metadata/federation

* The Metadata endpoint on the MSA (/matching-service/SAML2/metadata) now returns enough information for the RP service to only need to consume metadata from the MSA instead of directly from the Verify hub.
 - EntityDescriptor with entityID of the RP's matching service now contains an IDPSSODescriptor containing the matching service's signing cert. This exposes the MSA as an IDP to the RP - the final response to the RP service is generated by the MSA and it is effectively working as an IDP, this formalises that relationship.

* New requireHubCertificates configuration in the MSA's .yml file
 - Default is false, when enabled it re-publishes the Verify hub's EntityDescriptor in the MSA's metadata (i.e. for entityId https://signin.service.gov.uk).

* New hubSSOUri configuration in the MSA's .yml file
 - The MSA's metadata contains an IDPSSODescriptor SingleSignOnService element which RP's can use to retrieve the location that AuthnRequests should be sent to, instead of retrieving that information from the metadata on hub. This is configured via the hubSSOUri property.
 - for production this is https://www.signin.service.gov.uk/SAML2/SSO
 - for integration this is https://www.integration.signin.service.gov.uk/SAML2/SSO

* Added support for sending metrics to Graphite.
 - Follow official Dropwizard configuration: http://www.dropwizard.io/0.9.1/docs/manual/configuration.html#graphite-reporter

* Upgraded to Dropwizard 0.9.3
 - Remove acceptSelfSignedCerts from the configuration
 - Rename httpClient to matchingServiceClient in the configuration

February 10, 2016 -- v. 493
* Added support for a secondary MSA signing certificate via the `publicSecondarySigningKeyConfiguration` property in the configuration.
* To avoid downtime during signing key rotations, follow this procedure:
 - Add a `publicSecondarySigningKeyConfiguration`
 - Load the metadata produced by the MSA into the service endpoint (hub response consumer)
 - Update the `privateSigningKeyConfiguration` to sign using the new private key
 - Once the new key pair is in use everywhere, remove the old certificate key configuration.
* Metadata generated by the MSA at `/matching-service/SAML2/metadata` now includes the correct entityId
* Added dependency on SAML-Security and reduced duplication of classes

January 14, 2016 -- v. 482
* Added support for a secondary MSA encryption key via the `privateSecondaryEncryptionKeyConfiguration` property in the configuration.
* To avoid downtime during PKI rotations, move the old key to `privateSecondaryEncryptionKeyConfiguration` and put the new key in `privateEncryptionKeyConfiguration`.
* Removed dependency on SAML-lib.
* Fix: adding public signing certificates into the MSA's Metadata

November 6, 2015 -- v. 470
* Updated SAML-lib to 2.6.5-312, which uses OpenSAML 2.6.5, for #2893
* Some performances improvements have been made
* Truststore is updated to enable MSA to additionally trust messages verified by IDAP Core CA.
* Disabling option to disable encryption, `encryptionDisabled` property in the configuration is no longer valid.
* Upgrade to Java 1.8. MSA must be run with JDK 1.8 now.

Aug 12, 2015 -- v. 419
* Fix: an issue was identified and fixed where the MSA would leak open TCP connections if errors occured in the handling of a request.

Jul 28, 2015 -- v. 411
* Upgrade version of Dropwizard to 0.8.2
 * Includes fix for stale keep-alive connections
* Support for standard HTTP Proxies
 * Follow the official Oracle guide: http://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html
* Added MDC keys `messageId`, `entityId` and `logPrefix` to logback MDC to capture SAML message context
 * For `file` and `console` type logging appenders, we recommend you update your `logFormat` to include the `logPrefix` key, e.g. `'%-5p [%d{ISO8601,UTC}] %c: %X{logPrefix}%m%n%xEx'`
 * For `logstash-file` and `logstash-syslog` type logging appenders, the new MDC keys will be automatically added to logstash output as extra columns

~~~ Missing notes from wiki

Jun 25,2014 -- v. 247
* Removing type from NameID to support Shibboleth


Feb 28, 2014 -- v. 153

* Upgraded to Dropwizard 0.7rc1
* Jetty configuration has changed
* Graphite configuration has changed
* Breaking changes for configuration - see instructions

-- Upcoming Changes --

* Re-enable configurable syslog logging
* Add configurable logstash format for files or console logging
* Allow dropwizard configuration for log levels

Feb 3, 2014 -- v. 151

* Configuration changes to better support feature flags and readability
* Moved disableEncryption to featureFlagConfiguration
* Added logLevel [TRACE|DEBUG|INFO|WARN|ERROR]
* Added a HealthCheck and Graphite configuration to monitor MSA behaviour in production
configuration.yml
