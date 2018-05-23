package uk.gov.ida.matchingserviceadapter.mappers;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingServiceIdaStatus;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceResponseDtoBuilder.aMatchingServiceResponseDto;
import static uk.gov.ida.saml.core.domain.MatchingServiceIdaStatus.MatchingServiceMatch;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapperTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    private static final String ENTITY_ID = "entityId";
    private static final String TEST_ID = "testId";
    private static final String REQUEST_ID = "requestId";
    private static final String ASSERTION_CONSUMER_SERVICE_URL = "assertionConsumerServiceUrl";
    private static final String AUTHN_REQUEST_ISSUER_ID = "authnRequestIssuerId";
    private static final String HASH_PID = "hashPid";
    private MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper mapper;

    @Before
    public void setup(){
        DateTimeFreezer.freezeTime();
        mapper = new MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(configuration, assertionLifetimeConfiguration, idGenerator);
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.parse("30m"));
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
        when(idGenerator.getId()).thenReturn(TEST_ID);
    }

    @After
    public void after() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithMatch() {
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withMatch().build();
        OutboundResponseFromMatchingService responseFromMatchingService = mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID);

        assertThat(responseFromMatchingService.getStatus()).isEqualTo(MatchingServiceMatch);
        assertThat(responseFromMatchingService.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(responseFromMatchingService.getId()).isEqualTo(TEST_ID);
        assertThat(responseFromMatchingService.getIssuer()).isEqualTo(ENTITY_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion()).isPresent();
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions()).isNotNull();
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getId()).isEqualTo(TEST_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getIssuerId()).isEqualTo(ENTITY_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getNotOnOrAfter()).isEqualTo(DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()));
        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get().getAssertionRestrictions().getRecipient()).isEqualTo(ASSERTION_CONSUMER_SERVICE_URL);
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithNoMatch() {
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withNoMatch().build();

        OutboundResponseFromMatchingService idaResponse = mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID);

        assertThat(idaResponse.getStatus()).isEqualTo(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void map_shouldThrowExceptionIfNotNoMatchOrMatch() {
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withBadResponse().build();

        mapper.map(
            response,
            HASH_PID,
            REQUEST_ID,
            ASSERTION_CONSUMER_SERVICE_URL,
            AuthnContext.LEVEL_2,
            AUTHN_REQUEST_ISSUER_ID);
    }
}
