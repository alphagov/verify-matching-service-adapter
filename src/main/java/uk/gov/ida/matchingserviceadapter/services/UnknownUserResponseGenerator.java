package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;
import uk.gov.ida.saml.core.domain.PersistentId;

import javax.inject.Inject;
import java.util.List;

public class UnknownUserResponseGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(UnknownUserResponseGenerator.class);

    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;
    private final UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor;
    private final IdGenerator idGenerator;

    @Inject
    public UnknownUserResponseGenerator(
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor,
            IdGenerator idGenerator) {
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
        this.userAccountCreationAttributeExtractor = userAccountCreationAttributeExtractor;
        this.idGenerator = idGenerator;
    }

    public OutboundResponseFromUnknownUserCreationService getMatchingServiceResponse(
        final UnknownUserCreationResponseDto unknownUserCreationResponseDto,
        final String requestId,
        final String hashPid,
        final String assertionConsumerServiceUrl,
        final String authnRequestIssuerId,
        final AssertionData assertionData,
        final List<Attribute> requestedUserAccountCreationAttributes) {
        if (unknownUserCreationResponseDto.getResult().equalsIgnoreCase(UnknownUserCreationResponseDto.FAILURE)) {
            return OutboundResponseFromUnknownUserCreationService.createFailure(idGenerator.getId(), requestId, matchingServiceAdapterConfiguration.getEntityId());
        }

        List<Attribute> userAttributesForAccountCreation =
                userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(
                        requestedUserAccountCreationAttributes,
                        assertionData.getMatchingDataset(),
                        assertionData.getCycle3Data()
                );

        if (userAttributesForAccountCreation.isEmpty()) {
            return OutboundResponseFromUnknownUserCreationService.createNoAttributeFailure(idGenerator.getId(), requestId, matchingServiceAdapterConfiguration.getEntityId());
        }

        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
            DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
            requestId,
            assertionConsumerServiceUrl);

        MatchingServiceAssertion assertion = new MatchingServiceAssertion(
                idGenerator.getId(),
                matchingServiceAdapterConfiguration.getEntityId(),
                DateTime.now(),
                new PersistentId(hashPid),
                assertionRestrictions,
                MatchingServiceAuthnStatement.createIdaAuthnStatement(assertionData.getLevelOfAssurance()),
                authnRequestIssuerId,
                userAttributesForAccountCreation);

        return OutboundResponseFromUnknownUserCreationService.createSuccess(
            idGenerator.getId(),
            assertion,
            requestId,
            matchingServiceAdapterConfiguration.getEntityId()
        );
    }

}
