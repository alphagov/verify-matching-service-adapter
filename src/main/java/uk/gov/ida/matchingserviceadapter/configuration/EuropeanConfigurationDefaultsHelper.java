package uk.gov.ida.matchingserviceadapter.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jetty.util.TypeUtil.asList;

public class EuropeanConfigurationDefaultsHelper {

    private static final String[] ACCEPTABLE_HUB_CONNECTOR_IDS_PRODUCTION = new String[] {
        "https://www.signin.service.gov.uk/SAML2/metadata/connector",               // (AWS)
        "https://connector-node.london.verify.govsvc.uk/SAML2/ConnectorMetadata",   // (GSP)
        "https://eidas.signin.service.gov.uk/ConnectorMetadata",                    // (potential new #1)
        "https://connector.eidas.signin.service.gov.uk/ConnectorMetadata",          // (potential new #2)
        "https://connector-node.eidas.signin.service.gov.uk/ConnectorMetadata",     // (potential new #3)
    };

    private static final String[] ACCEPTABLE_HUB_CONNECTOR_IDS_INTEGRATION = new String[]{
        // TODO
    };

    public static Map<MatchingServiceAdapterEnvironment, List<String>> DEFAULT_ACCEPTABLE_HUB_CONNECTOR_IDS;

    static {
        DEFAULT_ACCEPTABLE_HUB_CONNECTOR_IDS = new HashMap<>();
        DEFAULT_ACCEPTABLE_HUB_CONNECTOR_IDS.put(MatchingServiceAdapterEnvironment.INTEGRATION, asList(ACCEPTABLE_HUB_CONNECTOR_IDS_INTEGRATION));
        DEFAULT_ACCEPTABLE_HUB_CONNECTOR_IDS.put(MatchingServiceAdapterEnvironment.PRODUCTION, asList(ACCEPTABLE_HUB_CONNECTOR_IDS_PRODUCTION));
    }

}
