package uk.gov.ida.matchingserviceadapter.services;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.AssertionClassifier;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;

import java.util.List;
import java.util.Optional;

import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.MDS_ASSERTION;

public class AttributeQueryService {

    private final AttributeQuerySignatureValidator attributeQuerySignatureValidator;
    private final InstantValidator instantValidator;
    private final VerifyAssertionService verifyAssertionService;
    private final EidasAssertionService eidasAssertionService;
    private final UserIdHashFactory userIdHashFactory;
    private final AssertionClassifier assertionClassifier;

    @Inject
    public AttributeQueryService(AttributeQuerySignatureValidator attributeQuerySignatureValidator,
                                 InstantValidator instantValidator,
                                 VerifyAssertionService verifyAssertionService,
                                 EidasAssertionService eidasAssertionService,
                                 UserIdHashFactory userIdHashFactory,
                                 String hubEntityId) {
        this.attributeQuerySignatureValidator = attributeQuerySignatureValidator;
        this.instantValidator = instantValidator;
        this.verifyAssertionService = verifyAssertionService;
        this.eidasAssertionService = eidasAssertionService;
        this.userIdHashFactory = userIdHashFactory;
        this.assertionClassifier = new AssertionClassifier(hubEntityId);
    }

    public void validate(AttributeQuery attributeQuery) {
        attributeQuerySignatureValidator.validate(attributeQuery);

        instantValidator.validate(attributeQuery.getIssueInstant(), "AttributeQueryRequest IssueInstant");
    }

    public void validateAssertions(String expectedInResponseTo, List<Assertion> assertions) {

        if (hasMdsAssertion(assertions)) {
            verifyAssertionService.validate(expectedInResponseTo, assertions);
        } else {
            eidasAssertionService.validate(expectedInResponseTo, assertions);
        }
    }

    private boolean hasMdsAssertion(List<Assertion> assertions) {
        return assertions.stream()
                    .map(assertionClassifier::getClassification)
                    .anyMatch(MDS_ASSERTION::equals);
    }

    public AssertionData getAssertionData(List<Assertion> decryptedAssertions) {
        if (isCountryAttributeQuery(decryptedAssertions)) {
            return eidasAssertionService.translate(decryptedAssertions);
        }
        return verifyAssertionService.translate(decryptedAssertions);
    }

    private boolean isCountryAttributeQuery(List<Assertion> assertions) {
        return assertions.stream().anyMatch(eidasAssertionService::isCountryAssertion);
    }

    public String hashPid(AssertionData assertionData) {
        return userIdHashFactory.hashId(assertionData.getMatchingDatasetIssuer(),
                assertionData.getMatchingDataset().getPersonalId(),
                Optional.ofNullable(assertionData.getLevelOfAssurance()));
    }
}
