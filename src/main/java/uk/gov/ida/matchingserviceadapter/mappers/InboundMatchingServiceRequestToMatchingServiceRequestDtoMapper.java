package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;

import static com.google.common.base.Optional.absent;

public class InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper {

    private final UserIdHashFactory userIdHashFactory;
    private final MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    @Inject
    public InboundMatchingServiceRequestToMatchingServiceRequestDtoMapper(
        UserIdHashFactory userIdHashFactory, MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
        MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper) {
        this.userIdHashFactory = userIdHashFactory;
        this.matchingDatasetToMatchingDatasetDtoMapper = matchingDatasetToMatchingDatasetDtoMapper;
    }

    public MatchingServiceRequestDto map(InboundMatchingServiceRequest attributeQuery) {
        IdentityProviderAssertion matchingDatasetAssertion = attributeQuery.getMatchingDatasetAssertion();
        IdentityProviderAssertion authnStatementAssertion = attributeQuery.getAuthnStatementAssertion();
        MatchingDataset matchingDataset = matchingDatasetAssertion.getMatchingDataset().get();

        final String hashedPid = userIdHashFactory.hashId(matchingDatasetAssertion.getIssuerId(),
            matchingDatasetAssertion.getPersistentId().getNameId(),
            authnStatementAssertion.getAuthnStatement().transform(IdentityProviderAuthnStatement::getAuthnContext));

        Optional<HubAssertion> cycle3AttributeAssertion = attributeQuery.getCycle3AttributeAssertion();
        Optional<Cycle3Dataset> cycle3Dataset = absent();
        if (cycle3AttributeAssertion.isPresent()) {
            cycle3Dataset = cycle3AttributeAssertion.get().getCycle3Data();
        }
        AuthnContext authnContext = authnStatementAssertion.getAuthnStatement().get().getAuthnContext();

        MatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.map(matchingDataset);

        LevelOfAssuranceDto levelOfAssurance = AuthnContextToLevelOfAssuranceDtoMapper.map(authnContext);

        return new MatchingServiceRequestDto(
                matchingDatasetDto,
                mapCycle3(cycle3Dataset),
                hashedPid,
                attributeQuery.getId(),
                levelOfAssurance);
    }

    private Optional<Cycle3DatasetDto> mapCycle3(Optional<Cycle3Dataset> cycle3Dataset) {
       if (!cycle3Dataset.isPresent()){
           return absent();
       }

       return Optional.fromNullable(Cycle3DatasetDto.createFromData(cycle3Dataset.get().getAttributes()));
    }
}
