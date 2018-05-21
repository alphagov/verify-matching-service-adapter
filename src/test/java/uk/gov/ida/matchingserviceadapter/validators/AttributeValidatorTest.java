package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Attribute;

import uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentGivenNameBuilder;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.SimpleStringAttributeBuilder;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeValidator.ATTRIBUTE_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeValidator.TOO_MANY_ATTRIBUTE_VALUES;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeValidator.ZERO_ATTRIBUTE_VALUES;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class AttributeValidatorTest {

    private Validator<Attribute> validator;
    private Validator<Attribute> nameValidator;

    @Before
    public void setup() {
        validator = new AttributeValidator<>(identity(), StringValueSamlObject.class, StringValidators.isNonEmpty(StringValueSamlObject::getValue));
        nameValidator = new AttributeValidator<>(identity(), CurrentGivenName.class, StringValidators.isNonEmpty(CurrentGivenName::getFirstName));
    }

    @Test
    public void shouldGenerateNoErrorsWhenAttributeIsValid() {
        Attribute attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(IdaConstants.Eidas_Attributes.FirstName.NAME).withSimpleStringValue("Joe").build();

        Messages messages = validator.validate(attribute, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenAttributeIsMissing() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(ATTRIBUTE_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAttributeHasMultipleAttributeValues() {
        Attribute attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute().withSimpleStringValue("foo").build();
        attribute.getAttributeValues().add(new OpenSamlXmlObjectFactory().createSimpleMdsAttributeValue("bar"));

        Messages messages = validator.validate(attribute, messages());

        assertThat(messages.hasErrorLike(TOO_MANY_ATTRIBUTE_VALUES)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAttributeHasEmptyAttributeValue() {
        Attribute attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(IdaConstants.Eidas_Attributes.FirstName.NAME).withSimpleStringValue("").build();

        Messages messages = validator.validate(attribute, messages());

        assertThat(messages.hasErrorLike(StringValidators.STRING_VALUE_IS_EMPTY)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAttributeHasMissingAttributeValue() {
        Attribute attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(IdaConstants.Eidas_Attributes.FirstName.NAME).build();

        Messages messages = validator.validate(attribute, messages());

        assertThat(messages.hasErrorLike(ZERO_ATTRIBUTE_VALUES)).isTrue();
    }

    @Test
    public void shouldGenerateNoErrorIfAttributeIsTransliterableAndHasTwoValues() {
        Attribute attribute = AttributeStatementBuilder.aCurrentGivenNameAttribute("Georgios", "Γεώργιος");

        Messages messages = nameValidator.validate(attribute, messages());

        assertThat(messages.getErrors()).isEmpty();
    }

    @Test
    public void shouldGenerateNoErrorIfAttributeIsTransliterableAndHasOneValue() {
        Attribute attribute = AttributeStatementBuilder.aCurrentGivenNameAttribute("Georgios");

        Messages messages = nameValidator.validate(attribute, messages());

        assertThat(messages.getErrors()).isEmpty();
    }

    @Test
    public void shouldGenerateErrorIfAttributeIsTransliterableAndHasMoreThanTwoValues() {
        Attribute attribute = AttributeStatementBuilder.aCurrentGivenNameAttribute("Georgios", "Γεώργιος");
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName("George");
        firstNameValue.setIsLatinScript(true);
        attribute.getAttributeValues().add(firstNameValue);

        Messages messages = nameValidator.validate(attribute, messages());

        assertThat(messages.hasErrorLike(TOO_MANY_ATTRIBUTE_VALUES)).isTrue();
    }
}