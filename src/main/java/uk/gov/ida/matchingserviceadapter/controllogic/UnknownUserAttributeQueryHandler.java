package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.configuration.AssertionLifetimeConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertionFactory;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttributeExtractor;
import uk.gov.ida.matchingserviceadapter.mappers.AuthnContextToLevelOfAssuranceDtoMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.core.domain.AssertionRestrictions;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.PersistentId;

import java.text.MessageFormat;
import java.util.List;

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

    public OutboundResponseFromUnknownUserCreationService handle(InboundMatchingServiceRequest attributeQuery) {
        IdentityProviderAssertion matchingDatasetAssertion = attributeQuery.getMatchingDatasetAssertion();
        IdentityProviderAssertion authnStatementAssertion = attributeQuery.getAuthnStatementAssertion();
        final String hashedPid = userIdHashFactory.hashId(matchingDatasetAssertion.getIssuerId(),
            matchingDatasetAssertion.getPersistentId().getNameId(),
            authnStatementAssertion.getAuthnStatement().transform(IdentityProviderAuthnStatement::getAuthnContext));

        LevelOfAssuranceDto levelOfAssurance = AuthnContextToLevelOfAssuranceDtoMapper.map(attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext());
        UnknownUserCreationResponseDto unknownUserCreationResponseDto = matchingServiceProxy.makeUnknownUserCreationRequest(new UnknownUserCreationRequestDto(hashedPid, levelOfAssurance));
        if (unknownUserCreationResponseDto.getResult().equalsIgnoreCase(UnknownUserCreationResponseDto.FAILURE)) {
            return OutboundResponseFromUnknownUserCreationService.createFailure(attributeQuery.getId(), matchingServiceAdapterConfiguration.getEntityId());
        }

        Optional<MatchingDataset> matchingDataset = attributeQuery.getMatchingDatasetAssertion().getMatchingDataset();
        List<Attribute> extractedUserAccountCreationAttributes = userAccountCreationAttributeExtractor.getUserAccountCreationAttributes(
                attributeQuery.getUserCreationAttributes(),
                matchingDataset,
                attributeQuery.getCycle3AttributeAssertion());

        final OutboundResponseFromUnknownUserCreationService matchingServiceResponse = getMatchingServiceResponse(attributeQuery, hashedPid, extractedUserAccountCreationAttributes);
        LOG.info(MessageFormat.format("Result from unknown attribute query request for id {0} is {1}", attributeQuery.getId(), matchingServiceResponse.getStatus()));

        return matchingServiceResponse;
    }

    private OutboundResponseFromUnknownUserCreationService getMatchingServiceResponse(final InboundMatchingServiceRequest attributeQuery, final String hashedPid, final List<Attribute> extractedUserAccountCreationAttributes) {
        final OutboundResponseFromUnknownUserCreationService matchingServiceResponse;
        if (!extractedUserAccountCreationAttributes.isEmpty()) {
            matchingServiceResponse = getMatchingServiceResponse(hashedPid, attributeQuery, extractedUserAccountCreationAttributes);
        } else {
            matchingServiceResponse = OutboundResponseFromUnknownUserCreationService.createNoAttributeFailure(attributeQuery.getId(), matchingServiceAdapterConfiguration.getEntityId());
        }
        return matchingServiceResponse;
    }

    private OutboundResponseFromUnknownUserCreationService getMatchingServiceResponse(
        final String hashPid,
        final InboundMatchingServiceRequest attributeQuery,
        final List<Attribute> userAttributesForAccountCreation) {
        AssertionRestrictions assertionRestrictions = new AssertionRestrictions(
            DateTime.now().plus(assertionLifetimeConfiguration.getAssertionLifetime().toMilliseconds()),
            attributeQuery.getId(),
            attributeQuery.getAssertionConsumerServiceUrl());

        MatchingServiceAssertion assertion = matchingServiceAssertionFactory.createAssertionFromMatchingService(
            new PersistentId(hashPid),
            matchingServiceAdapterConfiguration.getEntityId(),
            assertionRestrictions,
            attributeQuery.getAuthnStatementAssertion().getAuthnStatement().get().getAuthnContext(),
            attributeQuery.getAuthnRequestIssuerId(),
            userAttributesForAccountCreation);
        return OutboundResponseFromUnknownUserCreationService.createSuccess(
            assertion,
            attributeQuery.getId(),
            matchingServiceAdapterConfiguration.getEntityId()
        );
    }

}
