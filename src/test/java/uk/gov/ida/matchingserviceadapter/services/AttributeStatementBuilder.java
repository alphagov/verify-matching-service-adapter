package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.LocalDate;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.Gender;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentFamilyNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentGivenNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.DateOfBirthBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.GenderBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.PersonIdentifierBuilder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.ida.saml.core.IdaConstants.EIDAS_NATURAL_PERSON_NS;

public class AttributeStatementBuilder {

    public static final String BIRTH_NAME="http://eidas.europa.eu/attributes/naturalperson/BirthName";
    public static final String PLACE_OF_BIRTH="http://eidas.europa.eu/attributes/naturalperson/PlaceOfBirth";

    private static OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private List<Attribute> attributes = new ArrayList<>();

    public static AttributeStatementBuilder anAttributeStatement() {
        return new AttributeStatementBuilder();
    }

    /**
     * Retain original defaults to maintain backward compatibility with existing tests.
     * @deprecated Use @link AttributeStatementBuilder#anEidasAttributeStatement(Attribute ... attributes) instead for future tests.
     * @return
     */
    public static AttributeStatementBuilder anEidasAttributeStatement() {
        return anEidasAttributeStatement(
            aCurrentGivenNameAttribute("Joe"),
            aCurrentFamilyNameAttribute("Bloggs"),
            aPersonIdentifierAttribute("JB12345"),
            aDateOfBirthAttribute(LocalDate.now()));
    }

    public static AttributeStatementBuilder anEidasAttributeStatement(Attribute ... attributes) {
        return anAttributeStatement().addAllAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());
    }

    public static Attribute aCurrentGivenNameAttribute(String givenName) {
        Attribute firstNameAttr =  anAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME);
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName(givenName);
        firstNameAttr.getAttributeValues().add(firstNameValue);
        return firstNameAttr;
    }

    public static Attribute aCurrentFamilyNameAttribute(String familyName) {
        Attribute familyNameAttr =  anAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME);
        CurrentFamilyName familyNameValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameValue.setFamilyName(familyName);
        familyNameAttr.getAttributeValues().add(familyNameValue);
        return familyNameAttr;
    }

    public static Attribute aPersonIdentifierAttribute(String personIdentifier) {
        Attribute personIdentifierAttr =  anAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        PersonIdentifier personIdentifierValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierValue.setPersonIdentifier(personIdentifier);
        personIdentifierAttr.getAttributeValues().add(personIdentifierValue);
        return personIdentifierAttr;
    }

    public static Attribute aDateOfBirthAttribute(LocalDate dateOfBirth) {
        Attribute dateOfBirthAttr =  anAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
        DateOfBirth dateOfBirthValue = new DateOfBirthBuilder().buildObject();
        dateOfBirthValue.setDateOfBirth(dateOfBirth);
        dateOfBirthAttr.getAttributeValues().add(dateOfBirthValue);
        return dateOfBirthAttr;
    }

    public static Attribute aGenderAttribute(String gender) {
        Attribute genderAttribute =  anAttribute(IdaConstants.Eidas_Attributes.Gender.NAME);
        Gender genderValue = new GenderBuilder().buildObject();
        genderValue.setValue(gender);
        genderAttribute.getAttributeValues().add(genderValue);
        return genderAttribute;
    }

    public static Attribute aBirthNameAttribute(String birthName) {
        // TODO: Once type is created in ida-saml-extensions
        throw new UnsupportedOperationException();

//        Attribute birthNameAttribute =  anAttribute(BIRTH_NAME);
//        XSStringBuilder stringBuilder = new XSStringBuilder();
//        XSString birthNameAttributeValue = stringBuilder.buildObject(
////            new QName(SAMLConstants.SAML20_NS, AttributeValue.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX),
//            new QName(SAMLConstants.SAML20_NS, "AttributeValue", SAMLConstants.SAML20_PREFIX),
////            new QName("", "BirthNameType", IdaConstants.EIDAS_NATURUAL_PREFIX),
//            new QName(EIDAS_NATURAL_PERSON_NS, "BirthNameType", IdaConstants.EIDAS_NATURUAL_PREFIX)
//        );
//        birthNameAttributeValue.setValue(birthName);
//        birthNameAttribute.getAttributeValues().add(birthNameAttributeValue);
//        return birthNameAttribute;
    }

    public static Attribute aPlaceOfBirthAttribute(String birthName) {
        // TODO: Once type is created in ida-saml-extensions
        throw new UnsupportedOperationException();
    }

    private static Attribute anAttribute(String name) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(name);
        return attribute;
    }

    public AttributeStatement build() {
        AttributeStatement attributeStatement = openSamlXmlObjectFactory.createAttributeStatement();

        attributeStatement.getAttributes().addAll(attributes);

        return attributeStatement;
    }

    public AttributeStatementBuilder addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public AttributeStatementBuilder addAllAttributes(List<Attribute> attributes) {
        this.attributes.addAll(attributes);
        return this;
    }
}
