package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Test;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.StringValidators.STRING_VALUE_IS_EMPTY;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class StringValidatorsTest {

    @Test
    public void shouldGenerateNoErrorsWhenStringIsNonEmpty() {
        Validator<String> validator = StringValidators.isNonEmpty(identity());

        Messages messages = validator.validate("foo", messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenStringIsNull() {
        Validator<String> validator = StringValidators.isNonEmpty(identity());

        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(STRING_VALUE_IS_EMPTY)).isTrue();
    }

}