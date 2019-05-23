package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.domain.AssertionClassification;
import uk.gov.ida.matchingserviceadapter.domain.AssertionClassifier;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.IdpConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.AUTHN_ASSERTION;
import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.MDS_ASSERTION;
import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.CYCLE_3_ASSERTION;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;

public class VerifyAssertionService extends AssertionService {

    private String hubEntityId;
    private final VerifyMatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private AuthnContextFactory authnContextFactory = new AuthnContextFactory();
    private final AssertionClassifier assertionClassifier;

    public VerifyAssertionService(InstantValidator instantValidator,
                                  SubjectValidator subjectValidator,
                                  IdpConditionsValidator conditionsValidator,
                                  SamlAssertionsSignatureValidator hubSignatureValidator,
                                  Cycle3DatasetFactory cycle3DatasetFactory,
                                  String hubEntityId,
                                  VerifyMatchingDatasetUnmarshaller matchingDatasetUnmarshaller) {
        super(instantValidator, subjectValidator, conditionsValidator, hubSignatureValidator, cycle3DatasetFactory);
        this.hubEntityId = hubEntityId;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.assertionClassifier = new AssertionClassifier(hubEntityId);

    }

    @Override
    public void validate(String requestId, List<Assertion> assertions) {

        Map<AssertionClassification, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifier::getClassification));

        List<Assertion> authnAssertions = assertionMap.get(AUTHN_ASSERTION);
        if (authnAssertions == null || authnAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }

        List<Assertion> mdsAssertions = assertionMap.get(MDS_ASSERTION);
        if (mdsAssertions == null || mdsAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one matching dataset assertion is expected.");
        }

        Assertion authnAssertion = authnAssertions.get(0);
        Assertion mdsAssertion = mdsAssertions.get(0);

        validateHubAssertion(authnAssertion, requestId, hubEntityId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        validateHubAssertion(mdsAssertion, requestId, hubEntityId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        assertionMap.getOrDefault(CYCLE_3_ASSERTION, emptyList()).forEach(a -> validateCycle3Assertion(a, requestId, hubEntityId));

        if (!mdsAssertion.getIssuer().getValue().equals(authnAssertion.getIssuer().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_ISSUERS);
        }

        if (!mdsAssertion.getSubject().getNameID().getValue().equals(authnAssertion.getSubject().getNameID().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_PIDS);
        }
    }

    @Override
    public AssertionData translate(List<Assertion> assertions) {
        Map<AssertionClassification, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifier::getClassification));

        AuthnStatement authnStatement = assertionMap.get(AUTHN_ASSERTION).get(0).getAuthnStatements().get(0);
        String levelOfAssurance = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        Assertion mdsAssertion = assertionMap.get(MDS_ASSERTION).get(0);

        Optional<Assertion> cycle3Assertion = assertionMap.getOrDefault(CYCLE_3_ASSERTION, emptyList())
                .stream()
                .findFirst();

        return new AssertionData(mdsAssertion.getIssuer().getValue(),
                authnContextFactory.authnContextForLevelOfAssurance(levelOfAssurance),
                getCycle3Data(cycle3Assertion),
                matchingDatasetUnmarshaller.fromAssertion(mdsAssertion));
    }
}
