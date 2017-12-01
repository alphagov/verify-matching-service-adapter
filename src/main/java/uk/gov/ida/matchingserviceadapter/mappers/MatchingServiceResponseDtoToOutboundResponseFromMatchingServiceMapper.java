package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.util.ArrayList;

public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper {
    private final MatchingServiceAdapterConfiguration configuration;
    private final MatchingServiceAssertionFactory outboundAssertionFactory;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Inject
    public MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(
        MatchingServiceAdapterConfiguration configuration,
        MatchingServiceAssertionFactory outboundAssertionFactory,
        AssertionLifetimeConfiguration assertionLifetimeConfiguration) {

        this.configuration = configuration;
        this.outboundAssertionFactory = outboundAssertionFactory;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
    }

    public OutboundResponseFromMatchingService map(
        MatchingServiceResponseDto response,
        String hashPid,
        String requestId,
        String assertionConsumerServiceUrl,
        AuthnContext authnContext,
        String authnRequestIssuerId) {

        String result = response.getResult();

        switch (result) {
            case MatchingServiceResponseDto.MATCH:
                return getMatchResponse(hashPid, requestId, assertionConsumerServiceUrl, authnContext, authnRequestIssuerId);
            case MatchingServiceResponseDto.NO_MATCH:
                return OutboundResponseFromMatchingService.createNoMatchFromMatchingService(requestId, configuration.getEntityId());
            default:
                throw new UnsupportedOperationException("The matching service has returned an unsupported response message.");
        }
    }

    private OutboundResponseFromMatchingService getMatchResponse(
        final String hashPid,
        final String requestId,
        final String assertionConsumerServiceUrl,
        final AuthnContext authnContext,
        final String authnRequestIssuerId) {

        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
                DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
                requestId,
                assertionConsumerServiceUrl);

        MatchingServiceAssertion assertion = outboundAssertionFactory.createAssertionFromMatchingService(
                new PersistentId(hashPid),
                configuration.getEntityId(),
                assertionRestrictions,
                authnContext,
                authnRequestIssuerId,
                new ArrayList<>());
        return OutboundResponseFromMatchingService.createMatchFromMatchingService(
                assertion,
                requestId,
                configuration.getEntityId()
        );
    }
}
