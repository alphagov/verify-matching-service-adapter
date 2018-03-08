package uk.gov.ida.matchingserviceadapter.mappers;

import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingServiceIdaStatus;
import uk.gov.ida.saml.core.domain.PersistentId;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.builders.InboundMatchingServiceRequestBuilder.anInboundMatchingServiceRequest;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceAssertionBuilder.aMatchingServiceAssertion;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceResponseDtoBuilder.aMatchingServiceResponseDto;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;
import static uk.gov.ida.saml.core.test.builders.IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapperTest {

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Mock
    private MatchingServiceAssertionFactory assertionFactory;

    @Mock
    private AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Captor
    private ArgumentCaptor<AssertionRestrictions> assertionRestrictionsCaptor = null;

    private static final String ENTITY_ID = "entityId";
    private static final String HASH_PID = "hashPid";
    private MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper mapper;

    @Before
    public void setup(){
        DateTimeFreezer.freezeTime();
        mapper = new MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(configuration, assertionFactory, assertionLifetimeConfiguration);
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.parse("30m"));
        when(configuration.getEntityId()).thenReturn(ENTITY_ID);
    }

    @After
    public void after() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithMatch() {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion().build();
        IdentityProviderAuthnStatement authnStatement = anIdentityProviderAuthnStatement()
            .withAuthnContext(AuthnContext.LEVEL_2)
            .build();
        final String authnRequestIssuerEntityId = "authnRequestIssuerEntityId";
        InboundVerifyMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest()
                .withAssertionConsumerServiceUrl("/foo")
                .withRequestIssuerEntityId(authnRequestIssuerEntityId)
                .withAuthnStatementAssertion(anIdentityProviderAssertion().withAuthnStatement(authnStatement).build())
                .build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withMatch().build();
        when(assertionFactory.createAssertionFromMatchingService(
            any(PersistentId.class),
            eq(ENTITY_ID),
            assertionRestrictionsCaptor.capture(),
            eq(AuthnContext.LEVEL_2),
            eq(authnRequestIssuerEntityId),
            eq(new ArrayList<>()))).thenReturn(matchingServiceAssertion);

        OutboundResponseFromMatchingService responseFromMatchingService = mapper.map(
            response,
            HASH_PID,
            attributeQuery.getId(),
            attributeQuery.getAssertionConsumerServiceUrl(),
            attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(),
            attributeQuery.getAuthnRequestIssuerId());


        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get()).isEqualTo(matchingServiceAssertion);
        AssertionRestrictions actualAssertionRestriction = assertionRestrictionsCaptor.getValue();
        assertThat(actualAssertionRestriction).isNotNull();
        assertThat(actualAssertionRestriction.getInResponseTo()).isEqualTo(attributeQuery.getId());
        assertThat(actualAssertionRestriction.getNotOnOrAfter()).isEqualTo(DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()));
        assertThat(actualAssertionRestriction.getRecipient()).isEqualTo(attributeQuery.getAssertionConsumerServiceUrl());
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithNoMatch() {
        InboundVerifyMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest().build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withNoMatch().build();

        OutboundResponseFromMatchingService idaResponse = mapper.map(
            response,
            HASH_PID,
            attributeQuery.getId(),
            attributeQuery.getAssertionConsumerServiceUrl(),
            attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(),
            attributeQuery.getAuthnRequestIssuerId());

        assertThat(idaResponse.getStatus()).isEqualTo(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void map_shouldThrowExceptionIfNotNoMatchOrMatch() {
        InboundVerifyMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest().build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withCrappyResponse().build();

        mapper.map(
            response,
            HASH_PID,
            attributeQuery.getId(),
            attributeQuery.getAssertionConsumerServiceUrl(),
            attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(),
            attributeQuery.getAuthnRequestIssuerId());
    }
}
