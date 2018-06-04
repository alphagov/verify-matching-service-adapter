package uk.gov.ida.matchingserviceadapter.saml.factories;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalDate;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.extensions.Line;
import uk.gov.ida.saml.core.extensions.StringBasedMdsAttributeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;


public class UserAccountCreationAttributeFactory {

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    public UserAccountCreationAttributeFactory(OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {
        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    public Attribute createUserAccountCreationFirstNameAttribute(SimpleMdsValue<String> firstName) {
        return createPersonNameAttribute(firstName, UserAccountCreationAttribute.FIRST_NAME);
    }

    public Attribute createUserAccountCreationMiddleNameAttribute(SimpleMdsValue<String> middleName) {
        return createPersonNameAttribute(middleName, UserAccountCreationAttribute.MIDDLE_NAME);
    }

    public Attribute createUserAccountCreationSurnameAttribute(SimpleMdsValue<String> surname) {
        return createPersonNameAttribute(surname, UserAccountCreationAttribute.SURNAME);
    }

    public Attribute createUserAccountCreationDateOfBirthAttribute(SimpleMdsValue<LocalDate> dateOfBirth) {
        return createAttribute(
                UserAccountCreationAttribute.DATE_OF_BIRTH,
                singletonList(createAttributeValueWithDates(
                        openSamlXmlObjectFactory.createDateAttributeValue(dateOfBirth.getValue().toString("yyyy-MM-dd")),
                        dateOfBirth))
        );
    }

    public Attribute createUserAccountCreationCurrentAddressAttribute(Address address) {
        return createAttribute(
                UserAccountCreationAttribute.CURRENT_ADDRESS,
                singletonList(createAddressAttributeValue(address)));
    }

    public Attribute createUserAccountCreationAddressHistoryAttribute(List<Address> addresses) {
        List<AttributeValue> addressValues = new ArrayList<>();
        for (Address address : addresses) {
            addressValues.add(createAddressAttributeValue(address));
        }
        return createAttribute(
                UserAccountCreationAttribute.ADDRESS_HISTORY,
                addressValues);
    }

    public Attribute createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute userAccountCreationAttribute, boolean verified) {
        return createAttribute(userAccountCreationAttribute,
                ImmutableList.of(openSamlXmlObjectFactory.createVerifiedAttributeValue(verified))
        );
    }

    public Attribute createUserAccountCreationCycle3DataAttributes(Collection<String> cycle3Attributes) {
        List<AttributeValue> cycle3AttributeValues = new ArrayList<>();
        for (String cycle3Attribute : cycle3Attributes) {
            cycle3AttributeValues.add(openSamlXmlObjectFactory.createSimpleMdsAttributeValue(cycle3Attribute));
        }
        return createAttribute(UserAccountCreationAttribute.CYCLE_3, cycle3AttributeValues);
    }

    private Attribute createPersonNameAttribute(SimpleMdsValue<String> name, final UserAccountCreationAttribute userAccountCreationAttribute) {
        return createAttribute(
                userAccountCreationAttribute,
                singletonList(createAttributeValueWithDates(openSamlXmlObjectFactory.createPersonNameAttributeValue(name.getValue()), name))
        );
    }

    private AttributeValue createAddressAttributeValue(Address address) {

        uk.gov.ida.saml.core.extensions.Address addressAttributeValue = openSamlXmlObjectFactory.createAddressAttributeValue();
        addressAttributeValue.setFrom(address.getFrom());
        if (address.getTo().isPresent()) {
            addressAttributeValue.setTo(address.getTo().get());
        }
        for (String lineValue : address.getLines()) {
            Line line = openSamlXmlObjectFactory.createLine(lineValue);
            addressAttributeValue.getLines().add(line);
        }
        if (address.getPostCode().isPresent()) {
            addressAttributeValue.setPostCode(openSamlXmlObjectFactory.createPostCode(address.getPostCode().get()));
        }
        if (address.getInternationalPostCode().isPresent()) {
            addressAttributeValue.setInternationalPostCode(openSamlXmlObjectFactory.createInternationalPostCode(address.getInternationalPostCode().get()));
        }
        if (address.getUPRN().isPresent()) {
            addressAttributeValue.setUPRN(openSamlXmlObjectFactory.createUPRN(address.getUPRN().get()));
        }
        addressAttributeValue.setVerified(address.isVerified());

        return addressAttributeValue;
    }

    private AttributeValue createAttributeValueWithDates(StringBasedMdsAttributeValue attributeValue, SimpleMdsValue value) {
        attributeValue.setFrom(value.getFrom());
        attributeValue.setTo(value.getTo());
        return attributeValue;
    }

    private Attribute createAttribute(UserAccountCreationAttribute userAccountCreationAttribute, List<? extends XMLObject> attributeValues) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();

        String attributeName = userAccountCreationAttribute.getAttributeName();
        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeName);
        attribute.setNameFormat(Attribute.UNSPECIFIED);
        attribute.getAttributeValues().addAll(attributeValues);

        return attribute;
    }
}
