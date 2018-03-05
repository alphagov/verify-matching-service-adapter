package uk.gov.ida.matchingserviceadapter.factories;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;

public class AttributeQueryAttributeFactory {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Inject
    public AttributeQueryAttributeFactory(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    public Attribute createAttribute(final UserAccountCreationAttribute userAccountCreationAttribute) {
        final Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(userAccountCreationAttribute.getAttributeName());
        attribute.setNameFormat(Attribute.UNSPECIFIED);
        return attribute;
    }
}
