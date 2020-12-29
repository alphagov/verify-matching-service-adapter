package uk.gov.ida.matchingserviceadapter.builders;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class AttributeStatementBuilder {
    private static OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private List<Attribute> attributes = new ArrayList<>();

    public AttributeStatement build() {
        AttributeStatement attributeStatement = openSamlXmlObjectFactory.createAttributeStatement();

        attributeStatement.getAttributes().addAll(attributes);

        return attributeStatement;
    }
}
