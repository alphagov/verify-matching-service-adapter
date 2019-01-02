package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Assertion;

import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.AUTHN_ASSERTION;
import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.CYCLE_3_ASSERTION;
import static uk.gov.ida.matchingserviceadapter.domain.AssertionClassification.MDS_ASSERTION;

public class AssertionClassifier {
    private String hubEntityId;

    public AssertionClassifier(String hubEntityId) {
        this.hubEntityId = hubEntityId;
    }

    public AssertionClassification getClassification(Assertion assertion) {
        AssertionClassification classification = MDS_ASSERTION;

        if (!assertion.getAuthnStatements().isEmpty()) {
            classification = AUTHN_ASSERTION;
        } else if (assertion.getIssuer().getValue().equals(hubEntityId)) {
            classification = CYCLE_3_ASSERTION;
        }
        return classification;
    }
}
