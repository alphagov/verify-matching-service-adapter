package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.PredicatedValidator;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(MockitoJUnitRunner.class)
public class TimeRestrictionValidatorsTest {

    @Mock
    private DateTimeComparator dateTimeComparator;

    @Mock
    private Message message;

    @Test
    public void shouldGenerateErrorWhenDateTimeIsInThePast() {
        when(dateTimeComparator.isBeforeNow(any(DateTime.class))).thenReturn(true);
        PredicatedValidator<DateTime> validator = TimeRestrictionValidators.notInPastValidator(dateTimeComparator, identity(), message);

        Messages messages = validator.validate(DateTime.now(), messages());

        assertThat(messages.hasErrorLike(message)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenDateTimeIsInTheFuture() {
        when(dateTimeComparator.isAfterNow(any(DateTime.class))).thenReturn(true);
        PredicatedValidator<DateTime> validator = TimeRestrictionValidators.notInFutureValidator(dateTimeComparator, identity(), message);

        Messages messages = validator.validate(DateTime.now(), messages());

        assertThat(messages.hasErrorLike(message)).isTrue();
    }

}