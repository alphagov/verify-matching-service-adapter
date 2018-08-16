package uk.gov.ida.matchingserviceadapter.services;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchingResponseGeneratorTest {

    private static final String ENTITY_ID = "entityId";

    private MatchingResponseGenerator matchingResponseGenerator;

    @Mock
    private SoapMessageManager soapMessageManager;

    @Mock
    private Function<OutboundResponseFromMatchingService, Element> responseElementTransformer;

    @Mock
    private Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;

    @Mock
    private ManifestReader manifestReader;

    @Mock
    private MatchingServiceAdapterConfiguration matchingServiceConfiguration;

    @Before
    public void setUp() {
        JerseyGuiceUtils.reset();

        matchingResponseGenerator = new MatchingResponseGenerator(
                soapMessageManager,
                responseElementTransformer,
                healthCheckResponseTransformer,
                manifestReader,
                matchingServiceConfiguration
        );
    }

    @Test
    public void shouldGenerateCorrectHealthCheckResponse() throws IOException {
        Element responseValue = mock(Element.class);
        when(manifestReader.getAttributeValueFor(any(), any())).thenReturn("VERSION");
        when(matchingServiceConfiguration.isEidasEnabled()).thenReturn(true);
        when(matchingServiceConfiguration.shouldSignWithSHA1()).thenReturn(true);
        when(matchingServiceConfiguration.getEntityId()).thenReturn(ENTITY_ID);

        ArgumentCaptor<HealthCheckResponseFromMatchingService> healthCheckCaptor = ArgumentCaptor.forClass(HealthCheckResponseFromMatchingService.class);
        when(healthCheckResponseTransformer.apply(healthCheckCaptor.capture())).thenReturn(responseValue);
        Response response = matchingResponseGenerator.generateHealthCheckResponse("requestId");

        assertThat(response.getHeaders().getFirst("ida-msa-version")).isEqualTo("VERSION");

        String expectedRequestIdPhrase = "-version-VERSION-eidasenabled-true-shouldsignwithsha1-true";
        assertThat(healthCheckCaptor.getValue().getId()).endsWith(expectedRequestIdPhrase);
        assertThat(healthCheckCaptor.getValue().getInResponseTo()).isEqualTo("requestId");
        assertThat(healthCheckCaptor.getValue().getIssuer()).isEqualTo(ENTITY_ID);
    }
}