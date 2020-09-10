package uk.gov.ida.matchingserviceadapter.configuration;

import java.util.List;

import static org.eclipse.jetty.util.TypeUtil.asList;

public class EuropeanConfigurationDefaultsHelper {

    private static final String[] ACCEPTABLE_HUB_CONNECTOR_ENTITY_IDS_PRODUCTION = new String[] {
        "https://www.signin.service.gov.uk/SAML2/metadata/connector",
        "https://connector-node.eidas.signin.service.gov.uk/ConnectorMetadata",
    };

    private static final String[] ACCEPTABLE_HUB_CONNECTOR_ENTITY_IDS_INTEGRATION = new String[]{
        "https://www.integration.signin.service.gov.uk/SAML2/metadata/connector",
        "https://connector-node.integration.eidas.signin.service.gov.uk/ConnectorMetadata",
    };

    public static List<String> getDefaultAcceptableHubConnectorEntityIds(MatchingServiceAdapterEnvironment environment) {
        switch (environment) {
            case INTEGRATION:
                return asList(ACCEPTABLE_HUB_CONNECTOR_ENTITY_IDS_INTEGRATION);
            case PRODUCTION:
                return asList(ACCEPTABLE_HUB_CONNECTOR_ENTITY_IDS_PRODUCTION);
            default:
                throw new IllegalArgumentException(environment.name());
        }
    }

}
