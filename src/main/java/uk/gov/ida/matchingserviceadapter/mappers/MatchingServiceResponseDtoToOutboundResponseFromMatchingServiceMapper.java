package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;
import uk.gov.ida.saml.core.domain.PersistentId;

import static java.util.Collections.emptyList;

public class MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper {
    private final MatchingServiceAdapterConfiguration configuration;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;
    private final IdGenerator idGenerator;

    @Inject
    public MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper(
            MatchingServiceAdapterConfiguration configuration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            IdGenerator idGenerator) {

        this.configuration = configuration;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
        this.idGenerator = idGenerator;
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
                return OutboundResponseFromMatchingService.createNoMatchFromMatchingService(idGenerator.getId(), requestId, configuration.getEntityId());
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

        MatchingServiceAssertion assertion = new MatchingServiceAssertion(
                idGenerator.getId(),
                configuration.getEntityId(),
                DateTime.now(),
                new PersistentId(hashPid),
                assertionRestrictions,
                MatchingServiceAuthnStatement.createIdaAuthnStatement(authnContext),
                authnRequestIssuerId,
                emptyList());

        return OutboundResponseFromMatchingService.createMatchFromMatchingService(
                idGenerator.getId(),
                assertion,
                requestId,
                configuration.getEntityId()
        );
    }
}
