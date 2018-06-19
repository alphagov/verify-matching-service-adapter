package uk.gov.ida.integrationtest.helpers;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.saml.core.extensions.impl.VerifiedImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAccountCreationTestAssertionHelper {

    public static void assertThatResponseContainsExpectedUserCreationAttributes(List<AttributeStatement> attributeStatements, final List<Attribute> expectedUserCreationAttributes) {
        assertThat(attributeStatements).hasSize(1);
        AttributeStatement attributeStatement = attributeStatements.get(0);
        assertThat(attributeStatement.getAttributes()).hasSameSizeAs(expectedUserCreationAttributes);

        for (final Attribute expectedUserCreationAttribute : expectedUserCreationAttributes) {
            Attribute actualAttribute = attributeStatement.getAttributes().stream()
                .filter(attribute -> expectedUserCreationAttribute.getName().equals(attribute.getName()))
                .findFirst()
                .get();

            assertThat(actualAttribute.getAttributeValues()).hasSameSizeAs(expectedUserCreationAttribute.getAttributeValues());
            if (!actualAttribute.getAttributeValues().isEmpty()) {
                assertThatAttributeValuesAreEqual(actualAttribute.getAttributeValues().get(0), expectedUserCreationAttribute.getAttributeValues().get(0));
            }
        }
    }

    public static void assertThatAddressAttributeValuesAreEqual(AddressImpl actualValue, AddressImpl expectedValue) {
        assertThat(actualValue.getLines()).hasSameSizeAs(expectedValue.getLines());
        for (int i = 0; i < actualValue.getLines().size(); i++) {
            assertThat((actualValue.getLines().get(i).getValue())).isEqualTo(expectedValue.getLines().get(i).getValue());
        }

        assertThat(actualValue.getPostCode().getValue()).isEqualTo(expectedValue.getPostCode().getValue());
        assertThat(actualValue.getInternationalPostCode().getValue()).isEqualTo(expectedValue.getInternationalPostCode().getValue());
        assertThat(actualValue.getUPRN().getValue()).isEqualTo(expectedValue.getUPRN().getValue());
        assertThat(actualValue.getFrom()).isEqualTo(expectedValue.getFrom());
        assertThat(actualValue.getTo()).isEqualTo(expectedValue.getTo());
    }

    public static void assertThatAttributeValuesAreEqual(XMLObject actualValue, XMLObject expectedValue) {
        if (actualValue instanceof AddressImpl) {
            assertThatAddressAttributeValuesAreEqual((AddressImpl) actualValue, (AddressImpl) expectedValue);
        } else if (actualValue instanceof VerifiedImpl) {
            Verified actualAttributeValue = (Verified) actualValue;
            Verified expectedAttributeValue = (Verified) expectedValue;
            assertThat(actualAttributeValue.getValue()).isEqualTo(expectedAttributeValue.getValue());
        } else {
            StringValueSamlObject actualAttributeValue = (StringValueSamlObject) actualValue;
            StringValueSamlObject expectedAttributeValue = (StringValueSamlObject) expectedValue;
            assertThat(actualAttributeValue.getValue()).isEqualTo(expectedAttributeValue.getValue());
        }
    }
}
