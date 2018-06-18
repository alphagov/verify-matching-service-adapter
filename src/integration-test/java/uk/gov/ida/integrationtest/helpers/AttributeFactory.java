package uk.gov.ida.integrationtest.helpers;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;

public class AttributeFactory {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Inject
    public AttributeFactory(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    public Attribute createAttribute(final UserAccountCreationAttribute userAccountCreationAttribute) {
        final Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(userAccountCreationAttribute.getAttributeName());
        attribute.setNameFormat(Attribute.UNSPECIFIED);
        return attribute;
    }
}
