package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentFamilyNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aCurrentGivenNameAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aDateOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aGenderAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeStatementValidator.ATTRIBUTE_STATEMENT_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeStatementValidator.DATE_OF_BIRTH_IN_FUTURE;
import static uk.gov.ida.matchingserviceadapter.validators.MatchingElementValidator.NO_VALUE_MATCHING_FILTER;
import static uk.gov.ida.matchingserviceadapter.validators.StringValidators.STRING_VALUE_NOT_ENUMERATED;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

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
        Messages messages = validator.validate(anEidasAttributeStatement().addAttribute(aGenderAttribute("Male")).build(), messages());

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
            .addAttribute(aCurrentFamilyNameAttribute())
            .addAttribute(aPersonIdentifierAttribute())
            .addAttribute(aDateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfFamilyNameAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(aCurrentGivenNameAttribute())
            .addAttribute(aPersonIdentifierAttribute())
            .addAttribute(aDateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfPersonIdentifierAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(aCurrentGivenNameAttribute())
            .addAttribute(aCurrentFamilyNameAttribute())
            .addAttribute(aDateOfBirthAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfDateOfBirthAttributeIsMissing() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(aCurrentGivenNameAttribute())
            .addAttribute(aCurrentFamilyNameAttribute())
            .addAttribute(aPersonIdentifierAttribute())
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfDateOfBirthAttributeIsInTheFuture() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(aCurrentGivenNameAttribute())
            .addAttribute(aCurrentFamilyNameAttribute())
            .addAttribute(aPersonIdentifierAttribute())
            .addAttribute(aDateOfBirthAttribute(LocalDate.now().plusDays(1)))
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(DATE_OF_BIRTH_IN_FUTURE)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfGenderIsPresentAndInvalid() {
        AttributeStatement attributeStatement = anAttributeStatement()
            .addAttribute(aCurrentGivenNameAttribute())
            .addAttribute(aCurrentFamilyNameAttribute())
            .addAttribute(aPersonIdentifierAttribute())
            .addAttribute(aDateOfBirthAttribute())
            .addAttribute(aGenderAttribute("foo"))
            .build();

        Messages messages = validator.validate(attributeStatement, messages());

        assertThat(messages.hasErrorLike(STRING_VALUE_NOT_ENUMERATED)).isTrue();
    }
}
