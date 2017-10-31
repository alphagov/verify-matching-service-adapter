package uk.gov.ida.matchingserviceadapter.saml.security;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.saml.security.validators.ValidatedEncryptedAssertionContainer;

import java.util.List;
import java.util.stream.Collectors;

public class ValidatedAttributeQuery implements ValidatedEncryptedAssertionContainer {

    private final AttributeQuery attributeQuery;

    public ValidatedAttributeQuery(AttributeQuery attributeQuery) {
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

    public String getID() {
        return attributeQuery.getID();
    }

    public Issuer getIssuer() {
        return attributeQuery.getIssuer();
    }

    public Subject getSubject() {
        return attributeQuery.getSubject();
    }

    public DateTime getIssueInstant() {
        return attributeQuery.getIssueInstant();
    }

    public List<Attribute> getAttributes() {
        return attributeQuery.getAttributes();
    }
}
