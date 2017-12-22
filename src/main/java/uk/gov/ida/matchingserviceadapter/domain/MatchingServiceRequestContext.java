package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Document;

import java.util.List;

public class MatchingServiceRequestContext {

    private final Document attributeQueryDocument;
    private AttributeQuery attributeQuery;
    private List<Assertion> assertions;

    public MatchingServiceRequestContext(Document attributeQueryDocument, AttributeQuery attributeQuery, List<Assertion> assertions) {
        this.attributeQueryDocument = attributeQueryDocument;
        this.attributeQuery = attributeQuery;
        this.assertions = assertions;
    }

    public MatchingServiceRequestContext(Document attributeQueryDocument) {
        this.attributeQueryDocument = attributeQueryDocument;
    }

    public Document getAttributeQueryDocument() {
        return attributeQueryDocument;
    }

    public AttributeQuery getAttributeQuery() {
        return attributeQuery;
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }

    public void setAttributeQuery(AttributeQuery attributeQuery) {
        this.attributeQuery = attributeQuery;
    }

    public void setAssertions(List<Assertion> assertions) {
        this.assertions = assertions;
    }
}
