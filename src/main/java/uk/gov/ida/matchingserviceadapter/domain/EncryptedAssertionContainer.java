package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import uk.gov.ida.saml.security.validators.ValidatedEncryptedAssertionContainer;

import java.util.List;
import java.util.stream.Collectors;

/* Wrapper class to put an attribute query in to allow it to function as a ValidateEncryptedAssertionContainer */
public class EncryptedAssertionContainer implements ValidatedEncryptedAssertionContainer {
    private final AttributeQuery attributeQuery;

    public EncryptedAssertionContainer(AttributeQuery attributeQuery) {
        this.attributeQuery = attributeQuery;
    }

    @Override
    public List<EncryptedAssertion> getEncryptedAssertions() {
        return (List<EncryptedAssertion>) (List<?>)  attributeQuery.getSubject()
                .getSubjectConfirmations().stream()
                .flatMap(
                        s -> s.getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME).stream()
                )
                .collect(Collectors.toList());
    }
}
