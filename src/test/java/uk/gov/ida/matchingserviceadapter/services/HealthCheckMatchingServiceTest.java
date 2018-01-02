package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckMatchingServiceTest {

    private static final String ENTITY_ID = "entityId";
    private static final String REQUEST_ID = "requestId";
    private static final String VERSION = "version";

    @Mock
    private ManifestReader manifestReader;

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private MatchingServiceRequestContext requestContext;

    @Mock
    private AttributeQuery attributeQuery;

    private HealthCheckMatchingService service;

    @Before
    public void setup() {
        service = new HealthCheckMatchingService(manifestReader, configuration);
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
        when(requestContext.getAttributeQuery()).thenReturn(attributeQuery);
        when(attributeQuery.getID()).thenReturn(REQUEST_ID);
        when(manifestReader.getValue("Version-Number")).thenReturn(VERSION);
    }

    @Test
    public void shouldReturnHealthCheckResponse() {
        HealthCheckMatchingServiceResponse response = (HealthCheckMatchingServiceResponse) service.handle(requestContext);

        HealthCheckResponseFromMatchingService healthCheckResponse = response.getHealthCheckResponseFromMatchingService();
        assertThat(healthCheckResponse.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(healthCheckResponse.getIssuer()).isEqualTo(ENTITY_ID);
        assertThat(healthCheckResponse.getId()).contains(VERSION);
    }

}