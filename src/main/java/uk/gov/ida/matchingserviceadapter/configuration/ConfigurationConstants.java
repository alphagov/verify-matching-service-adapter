package uk.gov.ida.matchingserviceadapter.configuration;

import java.net.URI;

public interface ConfigurationConstants {
    String PRODUCTION_HUB_TRUSTSTORE_NAME = "prod_ida_hub_metadata.ts";
    String INTEGRATION_HUB_TRUSTSTORE_NAME = "test_ida_hub_metadata.ts";

    String PRODUCTION_IDP_TRUSTSTORE_NAME = "prod_ida_idp_metadata.ts";
    String INTEGRATION_IDP_TRUSTSTORE_NAME = "test_ida_idp_metadata.ts";

    String PRODUCTION_METADATA_TRUSTSTORE_NAME = "prod_metadata_truststore.ts";
    String INTEGRATION_METADATA_TRUSTSTORE_NAME = "test_metadata_truststore.ts";

    URI PRODUCTION_TRUST_ANCHOR_URI = URI.create("https://www.signin.service.gov.uk/SAML2/metadata/trust-anchor");
    URI INTEGRATION_TRUST_ANCHOR_URI = URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/trust-anchor");

    URI PRODUCTION_EIDAS_METADATA_SOURCE_URI = URI.create("https://www.signin.service.gov.uk/SAML2/metadata/aggregator");
    URI INTEGRATION_EIDAS_METADATA_SOURCE_URI = URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/aggregator");
}
