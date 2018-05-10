package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.matchingserviceadapter.domain.TranslatedAttributeQueryRequest;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.text.MessageFormat;
import java.util.List;

import static uk.gov.ida.matchingserviceadapter.mappers.AuthnContextToLevelOfAssuranceDtoMapper.map;

public class UnknownUserAttributeQueryHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UnknownUserAttributeQueryHandler.class);

    private final UserIdHashFactory userIdHashFactory;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final MatchingServiceAssertionFactory matchingServiceAssertionFactory;
    private final AssertionLifetimeConfiguration assertionLifetimeConfiguration;
    private final MatchingServiceProxy matchingServiceProxy;
    private final UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor;

    @Inject
    public UnknownUserAttributeQueryHandler(
            UserIdHashFactory userIdHashFactory,
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            MatchingServiceAssertionFactory matchingServiceAssertionFactory,
            AssertionLifetimeConfiguration assertionLifetimeConfiguration,
            MatchingServiceProxy matchingServiceProxy,
            UserAccountCreationAttributeExtractor userAccountCreationAttributeExtractor) {
        this.userIdHashFactory = userIdHashFactory;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.matchingServiceAssertionFactory = matchingServiceAssertionFactory;
        this.assertionLifetimeConfiguration = assertionLifetimeConfiguration;
        this.matchingServiceProxy = matchingServiceProxy;
        this.userAccountCreationAttributeExtractor = userAccountCreationAttributeExtractor;
    }

    public OutboundResponseFromUnknownUserCreationService createAccount(TranslatedAttributeQueryRequest attributeQuery, UnknownUserCreationResponseDto userCreationResponseDto) {
        if (userCreationResponseDto.getResult().equalsIgnoreCase(UnknownUserCreationResponseDto.FAILURE)) {
            return OutboundResponseFromUnknownUserCreationService.createFailure(attributeQuery.getMatchingServiceRequestDto().getMatchId(), matchingServiceAdapterConfiguration.getEntityId());
        }

        /*List<Attribute> extractedUserAccountCreationAttributes =
                userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(
                                attributeQuery.getUserCreationAttributes(),
                        matchingDataset.orElse(null), attributeQuery.getCycle3AttributeAssertion().orElse(null)
                );*/
        List<Attribute> extractedUserAccountCreationAttributes = attributeQuery.getUserAccountCreationAttributes();

        final OutboundResponseFromUnknownUserCreationService matchingServiceResponse = getMatchingServiceResponse(attributeQuery, attributeQuery.getMatchingServiceRequestDto().getHashedPid(), extractedUserAccountCreationAttributes);
        LOG.info(MessageFormat.format("Result from unknown attribute query request for id {0} is {1}", attributeQuery.getMatchingServiceRequestDto().getMatchId(), matchingServiceResponse.getStatus()));

        return matchingServiceResponse;
    }

    private OutboundResponseFromUnknownUserCreationService getMatchingServiceResponse(final TranslatedAttributeQueryRequest attributeQuery, final String hashedPid, final List<Attribute> extractedUserAccountCreationAttributes) {
        final OutboundResponseFromUnknownUserCreationService matchingServiceResponse;
        if (!extractedUserAccountCreationAttributes.isEmpty()) {
            matchingServiceResponse = getAccountCreationResponse(hashedPid, attributeQuery, extractedUserAccountCreationAttributes);
        } else {
            matchingServiceResponse = OutboundResponseFromUnknownUserCreationService.createNoAttributeFailure(attributeQuery.getMatchingServiceRequestDto().getMatchId(), matchingServiceAdapterConfiguration.getEntityId());
        }
        return matchingServiceResponse;
    }

    private OutboundResponseFromUnknownUserCreationService getAccountCreationResponse(
        final String hashPid,
        final TranslatedAttributeQueryRequest attributeQuery,
        final List<Attribute> userAttributesForAccountCreation) {
        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
            DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
            attributeQuery.getMatchingServiceRequestDto().getMatchId(),
            attributeQuery.getAssertionConsumerServiceUrl());

        MatchingServiceAssertion assertion = matchingServiceAssertionFactory.createAssertionFromMatchingService(
            new PersistentId(hashPid),
            matchingServiceAdapterConfiguration.getEntityId(),
            assertionRestrictions,
            map(attributeQuery.getMatchingServiceRequestDto().getLevelOfAssurance()),
            attributeQuery.getAuthnRequestIssuerId(),
            userAttributesForAccountCreation);
        return OutboundResponseFromUnknownUserCreationService.createSuccess(
            assertion,
            attributeQuery.getMatchingServiceRequestDto().getMatchId(),
            matchingServiceAdapterConfiguration.getEntityId()
        );
    }

}
