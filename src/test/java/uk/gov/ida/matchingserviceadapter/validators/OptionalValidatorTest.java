package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.StringValidators.STRING_VALUE_IS_EMPTY;

public class OptionalValidatorTest {

    private Validator<String> validator;

    @Before
    public void setup() {
        validator = new OptionalValidator<>(identity(), StringValidators.isNonEmpty(identity()));
    }

    @Test
    public void shouldGenerateNoErrorsIfValueIsMissing() {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateNoErrorsIfValueIsPresentAndValid() {
        Messages messages = validator.validate("foo", messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorIfValueIsPresentButInvalid() {
        Messages messages = validator.validate("", messages());

        assertThat(messages.hasErrorLike(STRING_VALUE_IS_EMPTY)).isTrue();
    }

}