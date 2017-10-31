package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.util.ArrayList;
import java.util.List;


public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper {
    private final MatchingServiceAdapterConfiguration configuration;
    private final MatchingServiceAssertionFactory outboundAssertionFactory;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;

    @Inject
    public MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(MatchingServiceAdapterConfiguration configuration, MatchingServiceAssertionFactory outboundAssertionFactory, AssertionLifetimeConfiguration assertionLifetimeConfiguration) {
        this.configuration = configuration;
        this.outboundAssertionFactory = outboundAssertionFactory;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
    }

    public OutboundResponseFromMatchingService map(MatchingServiceResponseDto response, String hashPid, InboundMatchingServiceRequest attributeQuery) {
        String result = response.getResult();

        switch (result) {
            case MatchingServiceResponseDto.MATCH:
                return getMatchResponse(hashPid, attributeQuery, new ArrayList<>());
            case MatchingServiceResponseDto.NO_MATCH:
                return OutboundResponseFromMatchingService.createNoMatchFromMatchingService(attributeQuery.getId(), configuration.getEntityId());
            default:
                throw new UnsupportedOperationException("The matching service has returned an unsupported response message.");
        }
    }

    private OutboundResponseFromMatchingService getMatchResponse(
            final String hashPid,
            final InboundMatchingServiceRequest attributeQuery,
            final List<Attribute> userAttributesForAccountCreation) {
        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
                DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
                attributeQuery.getId(),
                attributeQuery.getAssertionConsumerServiceUrl());

        MatchingServiceAssertion assertion = outboundAssertionFactory.createAssertionFromMatchingService(
                new PersistentId(hashPid),
                configuration.getEntityId(),
                assertionRestrictions,
                attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(),
                attributeQuery.getAuthnRequestIssuerId(),
                userAttributesForAccountCreation);
        return OutboundResponseFromMatchingService.createMatchFromMatchingService(
                assertion,
                attributeQuery.getId(),
                configuration.getEntityId()
        );
    }
}
