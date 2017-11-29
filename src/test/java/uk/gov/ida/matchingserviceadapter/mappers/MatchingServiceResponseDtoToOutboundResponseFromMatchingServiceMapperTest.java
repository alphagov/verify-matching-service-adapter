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
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
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
import static uk.gov.ida.saml.core.test.builders.AssertionRestrictionsBuilder.anAssertionRestrictions;
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


    private MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper mapper;

    @Before
    public void setup(){
        DateTimeFreezer.freezeTime();
        mapper = new MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(configuration, assertionFactory, assertionLifetimeConfiguration);
        when(assertionLifetimeConfiguration.getAssertionLifetime()).thenReturn(Duration.parse("30m"));
    }

    @After
    public void after() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithMatch() throws Exception {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion().build();
        AuthnContext levelOfAssurance = AuthnContext.LEVEL_2;
        IdentityProviderAuthnStatement authnStatement = anIdentityProviderAuthnStatement()
                .withAuthnContext(levelOfAssurance)
                .build();
        String authnRequestIssuerEntityId = "issuer-id";
        InboundMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest()
                .withAssertionConsumerServiceUrl("/foo")
                .withRequestIssuerEntityId(authnRequestIssuerEntityId)
                .withAuthnStatementAssertion(anIdentityProviderAssertion().withAuthnStatement(authnStatement).build())
                .build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withMatch().build();
        String issuerId = "issue";
        String hashPid = "apid";
        when(configuration.getEntityId()).thenReturn(issuerId);
        AssertionRestrictions expectedAssertionRestrictions =
                anAssertionRestrictions()
                        .withInResponseTo(attributeQuery.getId())
                        .withRecipient(attributeQuery.getAssertionConsumerServiceUrl())
                        .withNotOnOrAfter(DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()))
                        .build();
        when(assertionFactory.createAssertionFromMatchingService(any(PersistentId.class), eq(issuerId), assertionRestrictionsCaptor.capture(), eq(levelOfAssurance), eq(authnRequestIssuerEntityId), eq(new ArrayList<Attribute>())))
                .thenReturn(matchingServiceAssertion);

        OutboundResponseFromMatchingService responseFromMatchingService = mapper.map(response, hashPid,  attributeQuery.getId(), attributeQuery.getAssertionConsumerServiceUrl(), attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(), attributeQuery.getAuthnRequestIssuerId());


        assertThat(responseFromMatchingService.getMatchingServiceAssertion().get()).isEqualTo(matchingServiceAssertion);
        AssertionRestrictions actualAssertionRestriction = assertionRestrictionsCaptor.getValue();
        assertThat(actualAssertionRestriction).isNotNull();

        assertThat(actualAssertionRestriction.getInResponseTo()).isEqualTo(expectedAssertionRestrictions.getInResponseTo());
        assertThat(actualAssertionRestriction.getNotOnOrAfter()).isEqualTo(expectedAssertionRestrictions.getNotOnOrAfter());
        assertThat(actualAssertionRestriction.getRecipient()).isEqualTo(expectedAssertionRestrictions.getRecipient());
    }

    @Test
    public void map_shouldTranslateMatchingServiceResponseDtoToIdaResponseFromMatchingServiceWithNoMatch() throws Exception {
        InboundMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest().build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withNoMatch().build();
        String issuerId = "issue";
        when(configuration.getEntityId()).thenReturn(issuerId);

        OutboundResponseFromMatchingService idaResponse = mapper.map(response, "hashedpid", attributeQuery.getId(), attributeQuery.getAssertionConsumerServiceUrl(), attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(), attributeQuery.getAuthnRequestIssuerId());

        assertThat(idaResponse.getStatus()).isEqualTo(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void map_shouldThrowExceptionIfNotNoMatchOrMatch() throws Exception {
        InboundMatchingServiceRequest attributeQuery = anInboundMatchingServiceRequest().build();
        MatchingServiceResponseDto response = aMatchingServiceResponseDto().withCrappyResponse().build();
        String issuerId = "issue";
        when(configuration.getEntityId()).thenReturn(issuerId);

        mapper.map(response, "hashedpid",  attributeQuery.getId(), attributeQuery.getAssertionConsumerServiceUrl(), attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(), attributeQuery.getAuthnRequestIssuerId());

    }
}
