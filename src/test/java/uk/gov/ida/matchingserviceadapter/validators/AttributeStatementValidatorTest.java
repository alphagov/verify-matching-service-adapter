package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.Validator;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
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
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeStatementValidator.ATTRIBUTE_STATEMENT_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeStatementValidator.DATE_OF_BIRTH_IN_FUTURE;
import static uk.gov.ida.matchingserviceadapter.validators.MatchingElementValidator.NO_VALUE_MATCHING_FILTER;
import static uk.gov.ida.matchingserviceadapter.validators.StringValidators.STRING_VALUE_NOT_ENUMERATED;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;

@RunWith(OpenSAMLMockitoRunner.class)
public class AttributeStatementValidatorTest {

    private Validator<AttributeStatement> validator;

    @Before
    public void setup() {
        validator = new AttributeStatementValidator<>(identity());
    }

    @Test
    public void shouldGenerateNoErrorsIfAttributeStatementIsValid() {
        Messages messages = validator.validate(anEidasAttributeStatement().build(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateNoErrorsIfAttributeStatementIsValidAndHasGenderAttribute() {
        Messages messages = validator.validate(anEidasAttributeStatement().addAttribute(genderAttribute("Male")).build(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorIfAttributeStatementIsMissing() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(ATTRIBUTE_STATEMENT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfFirstNameAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(familyNameAttribute())
            .addAttribute(personIdentifierAttribute())
            .addAttribute(dateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfFamilyNameAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(firstNameAttribute())
            .addAttribute(personIdentifierAttribute())
            .addAttribute(dateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfPersonIdentifierAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(firstNameAttribute())
            .addAttribute(familyNameAttribute())
            .addAttribute(dateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfDateOfBirthAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(firstNameAttribute())
            .addAttribute(familyNameAttribute())
            .addAttribute(personIdentifierAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfDateOfBirthAttributeIsInTheFuture() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(firstNameAttribute())
            .addAttribute(familyNameAttribute())
            .addAttribute(personIdentifierAttribute())
            .addAttribute(dateOfBirthAttribute(LocalDate.now().plusDays(1)))
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(DATE_OF_BIRTH_IN_FUTURE)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfGenderIsPresentAndInvalid() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(firstNameAttribute())
            .addAttribute(familyNameAttribute())
            .addAttribute(personIdentifierAttribute())
            .addAttribute(dateOfBirthAttribute())
            .addAttribute(genderAttribute("foo"))
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(STRING_VALUE_NOT_ENUMERATED)).isTrue();
    }

    private Attribute firstNameAttribute() {
        Attribute firstName =  anAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME);
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName("Joe");
        firstName.getAttributeValues().add(firstNameValue);
        return firstName;
    }

    private Attribute familyNameAttribute() {
        Attribute familyName =  anAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME);
        CurrentFamilyName familyNameValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameValue.setFamilyName("Bloggs");
        familyName.getAttributeValues().add(familyNameValue);
        return familyName;
    }

    private Attribute personIdentifierAttribute() {
        Attribute personIdentifier =  anAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        PersonIdentifier personIdentifierValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierValue.setPersonIdentifier("JB12345");
        personIdentifier.getAttributeValues().add(personIdentifierValue);
        return personIdentifier;
    }

    private Attribute dateOfBirthAttribute(LocalDate dob) {
        Attribute dateOfBirth =  anAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
        DateOfBirth dateOfBirthValue = new DateOfBirthBuilder().buildObject();
        dateOfBirthValue.setDateOfBirth(dob);
        dateOfBirth.getAttributeValues().add(dateOfBirthValue);
        return dateOfBirth;
    }

    private Attribute genderAttribute(String genderType) {
        Attribute gender =  anAttribute(IdaConstants.Eidas_Attributes.Gender.NAME);
        Gender genderValue = new GenderBuilder().buildObject();
        genderValue.setValue(genderType);
        gender.getAttributeValues().add(genderValue);
        return gender;
    }

    private Attribute dateOfBirthAttribute() {
        return dateOfBirthAttribute(LocalDate.now());
    }

    private Attribute anAttribute(String name) {
        Attribute attribute = new OpenSamlXmlObjectFactory().createAttribute();
        attribute.setName(name);
        return attribute;
    }

}