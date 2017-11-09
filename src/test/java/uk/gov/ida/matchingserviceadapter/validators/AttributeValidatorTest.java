package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.SimpleStringAttributeBuilder;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeValidator.ATTRIBUTE_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.AttributeValidator.WRONG_NUMBER_OF_ATTRIBUTE_VALUES;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class AttributeValidatorTest {

    private Validator<Attribute> validator;

    @Before
    public void setup() {
        validator = new AttributeValidator<>(identity(), StringValueSamlObject.class, StringValidators.isNonEmpty(StringValueSamlObject::getValue));
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

        assertThat(messages.hasErrorLike(WRONG_NUMBER_OF_ATTRIBUTE_VALUES)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenAttributeHasEmptyAttributeValue() {
        Attribute attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute().withName(IdaConstants.Eidas_Attributes.FirstName.NAME).withSimpleStringValue("").build();

        Messages messages = validator.validate(attribute, messages());

        assertThat(messages.hasErrorLike(StringValidators.STRING_VALUE_IS_EMPTY)).isTrue();
    }

}