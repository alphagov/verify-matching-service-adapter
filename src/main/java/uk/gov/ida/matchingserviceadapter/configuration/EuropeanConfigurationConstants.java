package uk.gov.ida.matchingserviceadapter.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jetty.util.TypeUtil.asList;

public class EuropeanConfigurationConstants {

    private static String[] ACCEPTABLE_HUB_CONNECTOR_IDS_PRODUCTION = new String[] {
        "https://www.signin.service.gov.uk/SAML2/metadata/connector",               // (AWS)
        "https://connector-node.london.verify.govsvc.uk/SAML2/ConnectorMetadata",   // (GSP)
        "https://eidas.signin.service.gov.uk/ConnectorMetadata",                    // (potential new #1)
        "https://connector.eidas.signin.service.gov.uk/ConnectorMetadata",          // (potential new #2)
        "https://connector-node.eidas.signin.service.gov.uk/ConnectorMetadata",     // (potential new #3)
    };

    private static String[] ACCEPTABLE_HUB_CONNECTOR_IDS_INTEGRATION = new String[]{
        // TODO
    };

    private static Map<MatchingServiceAdapterEnvironment, List<String>> defaultAcceptableHubConnectorIds;

    public static Map<MatchingServiceAdapterEnvironment, List<String>> getDefaultAcceptableHubConnectorIds() {
        if (defaultAcceptableHubConnectorIds == null) {
            defaultAcceptableHubConnectorIds = new HashMap<>();
            defaultAcceptableHubConnectorIds.put(MatchingServiceAdapterEnvironment.INTEGRATION, asList(ACCEPTABLE_HUB_CONNECTOR_IDS_INTEGRATION));
            defaultAcceptableHubConnectorIds.put(MatchingServiceAdapterEnvironment.PRODUCTION, asList(ACCEPTABLE_HUB_CONNECTOR_IDS_PRODUCTION));
        }
        return defaultAcceptableHubConnectorIds;
    }

}
