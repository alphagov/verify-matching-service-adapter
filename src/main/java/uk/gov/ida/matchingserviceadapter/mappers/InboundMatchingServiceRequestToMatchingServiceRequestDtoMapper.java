package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.inject.Inject;
import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.matchingserviceadapter.rest.UniversalMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundEidasMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

import java.util.Optional;

public class InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper {

    private final UserIdHashFactory userIdHashFactory;
    private final MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    @Inject
    public InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(
        UserIdHashFactory userIdHashFactory,
        MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper) {
        this.userIdHashFactory = userIdHashFactory;
        this.matchingDatasetToMatchingDatasetDtoMapper = matchingDatasetToMatchingDatasetDtoMapper;
    }

    public VerifyMatchingServiceRequestDto map(InboundVerifyMatchingServiceRequest attributeQuery) {
        IdentityProviderAssertion matchingDatasetAssertion = attributeQuery.getMatchingDatasetAssertion();
        IdentityProviderAssertion authnStatementAssertion = attributeQuery.getAuthnStatementAssertion();
        MatchingDataset matchingDataset = matchingDatasetAssertion.getMatchingDataset().get();

        String hashedPid = userIdHashFactory.hashId(matchingDatasetAssertion.getIssuerId(),
            matchingDatasetAssertion.getPersistentId().getNameId(),
            authnStatementAssertion.getAuthnStatement().map(IdentityProviderAuthnStatement::getAuthnContext));

        AuthnContext authnContext = authnStatementAssertion.getAuthnStatement().get().getAuthnContext();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);
        LevelOfAssuranceDto levelOfAssurance = AuthnContextToLevelOfAssuranceDtoMapper.map(authnContext);

        return new VerifyMatchingServiceRequestDto(
                matchingDatasetDto,
                extractCycle3Dataset(attributeQuery),
                hashedPid,
                attributeQuery.getId(),
                levelOfAssurance);
    }

    public UniversalMatchingServiceRequestDto map(InboundEidasMatchingServiceRequest attributeQuery) {
        ProxyNodeAssertion proxyNodeAssertion = attributeQuery.getMatchingDatasetAssertion();
        EidasMatchingDataset matchingDataset = proxyNodeAssertion.getEidasMatchingDataset();

        LevelOfAssurance levelOfAssurance = proxyNodeAssertion.getLevelOfAssurance();
        AuthnContext authnContext = levelOfAssurance.toVerifyLevelOfAssurance();

        String issuer = proxyNodeAssertion.getIssuer();
        String personIdentifier = proxyNodeAssertion.getPersonIdentifier();
        String hashedPid = userIdHashFactory.hashId(issuer, personIdentifier, Optional.of(authnContext));

        UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset);
        LevelOfAssuranceDto levelOfAssuranceDto = AuthnContextToLevelOfAssuranceDtoMapper.map(authnContext);

        return new UniversalMatchingServiceRequestDto(
                matchingDatasetDto,
                extractCycle3Dataset(attributeQuery),
                hashedPid,
                attributeQuery.getId(),
                levelOfAssuranceDto);
    }

    private Optional<Cycle3DatasetDto> extractCycle3Dataset(InboundMatchingServiceRequest attributeQuery) {
        Optional<HubAssertion> cycle3AttributeAssertion = attributeQuery.getCycle3AttributeAssertion();
        if (!cycle3AttributeAssertion.isPresent()) {
           return Optional.empty();
        }

       return Optional.of(Cycle3DatasetDto.createFromData(cycle3AttributeAssertion.get().getCycle3Data().get().getAttributes()));
    }
}
