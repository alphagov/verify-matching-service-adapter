package uk.gov.ida.matchingserviceadapter.configuration;

import io.dropwizard.client.JerseyClientConfiguration;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.saml.metadata.FileBackedTrustStoreConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreBackedMetadataConfiguration;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CountryConfigurationTest {

    private static final String HUB_CONNECTOR_ENTITY_ID = "hubConnectorEntityId";
    private static final TrustStoreBackedMetadataConfiguration METADATA_CONFIGURATION = new TrustStoreBackedMetadataConfiguration(
        URI.create("uri"),
        10L,
        100L,
        "expectedEntityId",
        new JerseyClientConfiguration(),
        "jerseyClientName",
        "hubFederationId",
        new FileBackedTrustStoreConfiguration()
    );
    private CountryConfiguration countryConfiguration;

    @Before
    public void setUp() throws Exception {
        countryConfiguration = new CountryConfiguration(HUB_CONNECTOR_ENTITY_ID, METADATA_CONFIGURATION);
    }

    @Test
    public void getHubConnectorEntityId() throws Exception {
        assertThat(countryConfiguration.getHubConnectorEntityId()).isEqualTo(HUB_CONNECTOR_ENTITY_ID);
    }

    @Test
    public void getMetadata() throws Exception {
        assertThat(countryConfiguration.getMetadata()).isEqualTo(METADATA_CONFIGURATION);
    }
}
