package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.LocalDate;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.eidas.BirthName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.Gender;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;
import uk.gov.ida.saml.core.extensions.eidas.PlaceOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.impl.BirthNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentFamilyNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentGivenNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.DateOfBirthBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.GenderBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.PersonIdentifierBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.PlaceOfBirthBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttributeStatementBuilder {
    private static OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private List<Attribute> attributes = new ArrayList<>();

    public static AttributeStatementBuilder anAttributeStatement() {
        return new AttributeStatementBuilder();
    }

    public static AttributeStatementBuilder anEidasAttributeStatement(Attribute ... attributes) {
        return anAttributeStatement().addAllAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());
    }

    public static Attribute aCurrentGivenNameAttribute(){
        return aCurrentGivenNameAttribute("Joe");
    }

    public static Attribute aCurrentGivenNameAttribute(String givenName) {
        Attribute firstNameAttr =  anAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME);
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName(givenName);
        firstNameAttr.getAttributeValues().add(firstNameValue);
        return firstNameAttr;
    }

    public static Attribute aCurrentFamilyNameAttribute(){
        return aCurrentFamilyNameAttribute("Bloggs");
    }

    public static Attribute aCurrentFamilyNameAttribute(String familyName) {
        Attribute familyNameAttr =  anAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME);
        CurrentFamilyName familyNameValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameValue.setFamilyName(familyName);
        familyNameAttr.getAttributeValues().add(familyNameValue);
        return familyNameAttr;
    }

    public static Attribute aPersonIdentifierAttribute(){
        return aPersonIdentifierAttribute("JB12345");
    }

    public static Attribute aPersonIdentifierAttribute(String personIdentifier) {
        Attribute personIdentifierAttr =  anAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        PersonIdentifier personIdentifierValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierValue.setPersonIdentifier(personIdentifier);
        personIdentifierAttr.getAttributeValues().add(personIdentifierValue);
        return personIdentifierAttr;
    }

    public static Attribute aDateOfBirthAttribute(){
        return aDateOfBirthAttribute(LocalDate.now());
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
        Attribute birthNameAttribute =  anAttribute(IdaConstants.Eidas_Attributes.BirthName.NAME);
        BirthName birthNameValue = new BirthNameBuilder().buildObject();
        birthNameValue.setBirthName(birthName);
        birthNameAttribute.getAttributeValues().add(birthNameValue);
        return birthNameAttribute;
    }

    public static Attribute aPlaceOfBirthAttribute(String placeOfBirth) {
        Attribute placeOfBirthAttribute =  anAttribute(IdaConstants.Eidas_Attributes.PlaceOfBirth.NAME);
        PlaceOfBirth placeOfBirthValue = new PlaceOfBirthBuilder().buildObject();
        placeOfBirthValue.setPlaceOfBirth(placeOfBirth);
        placeOfBirthAttribute.getAttributeValues().add(placeOfBirthValue);
        return placeOfBirthAttribute;
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
