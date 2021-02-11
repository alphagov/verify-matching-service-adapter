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

public class AttributeQueryService {

    private final AttributeQuerySignatureValidator attributeQuerySignatureValidator;
    private final InstantValidator instantValidator;
    private final VerifyAssertionService verifyAssertionService;
    private final UserIdHashFactory userIdHashFactory;
    private final AssertionClassifier assertionClassifier;

    @Inject
    public AttributeQueryService(AttributeQuerySignatureValidator attributeQuerySignatureValidator,
                                 InstantValidator instantValidator,
                                 VerifyAssertionService verifyAssertionService,
                                 UserIdHashFactory userIdHashFactory,
                                 String hubEntityId) {
        this.attributeQuerySignatureValidator = attributeQuerySignatureValidator;
        this.instantValidator = instantValidator;
        this.verifyAssertionService = verifyAssertionService;
        this.userIdHashFactory = userIdHashFactory;
        this.assertionClassifier = new AssertionClassifier(hubEntityId);
    }

    public void validate(AttributeQuery attributeQuery) {
        attributeQuerySignatureValidator.validate(attributeQuery);

        instantValidator.validate(attributeQuery.getIssueInstant(), "AttributeQueryRequest IssueInstant");
    }

    public void validateAssertions(String expectedInResponseTo, List<Assertion> assertions) {
        verifyAssertionService.validate(expectedInResponseTo, assertions);
    }

    public AssertionData getAssertionData(List<Assertion> decryptedAssertions) {
        return verifyAssertionService.translate(decryptedAssertions);
    }

    public String hashPid(AssertionData assertionData) {
        return userIdHashFactory.hashId(assertionData.getMatchingDatasetIssuer(),
                assertionData.getMatchingDataset().getPersonalId(),
                Optional.ofNullable(assertionData.getLevelOfAssurance()));
    }
}
