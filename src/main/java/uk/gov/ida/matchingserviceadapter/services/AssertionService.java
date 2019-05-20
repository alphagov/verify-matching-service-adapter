package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.VerifyConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.domain.Cycle3Dataset;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.core.validation.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public abstract class AssertionService {
    protected final InstantValidator instantValidator;
    protected final SubjectValidator subjectValidator;
    protected final ConditionsValidator conditionsValidator;
    private final SamlAssertionsSignatureValidator hubSignatureValidator;
    private final Cycle3DatasetFactory cycle3DatasetFactory;
    private final AssertionAttributeStatementValidator attributeStatementValidator;

    protected AssertionService(InstantValidator instantValidator,
                               SubjectValidator subjectValidator,
                               ConditionsValidator conditionsValidator,
                               SamlAssertionsSignatureValidator hubSignatureValidator,
                               Cycle3DatasetFactory cycle3DatasetFactory) {
        this.instantValidator = instantValidator;
        this.subjectValidator = subjectValidator;
        this.conditionsValidator = conditionsValidator;
        this.hubSignatureValidator = hubSignatureValidator;
        this.cycle3DatasetFactory = cycle3DatasetFactory;
        this.attributeStatementValidator = new AssertionAttributeStatementValidator();
    }

    abstract void validate(String requestId, List<Assertion> assertions);

    abstract AssertionData translate(List<Assertion> assertions);

    protected void validateHubAssertion(Assertion assertion,
                                        String expectedInResponseTo,
                                        String hubEntityId,
                                        QName role) {

        if (assertion.getIssueInstant() == null) {
            throw new SamlResponseValidationException("Assertion IssueInstant is missing.");
        }

        if (assertion.getID() == null || assertion.getID().length() == 0) {
            throw new SamlResponseValidationException("Assertion Id is missing or blank.");
        }

        if (assertion.getIssuer() == null || assertion.getIssuer().getValue() == null || assertion.getIssuer().getValue().length() == 0) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing or blank Issuer.");
        }

        if (assertion.getVersion() == null) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing Version.");
        }

        if (!assertion.getVersion().equals(SAMLVersion.VERSION_20)) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " declared an illegal Version attribute value.");
        }

        hubSignatureValidator.validate(singletonList(assertion), role);
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);
    }

    protected void validateCycle3Assertion(Assertion assertion, String requestId, String hubEntityId) {
        validateHubAssertion(assertion, requestId, hubEntityId, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    protected Optional<Cycle3Dataset> getCycle3Data(Optional<Assertion> assertion) {
        return assertion.map(cycle3DatasetFactory::createCycle3DataSet);
    }
}
