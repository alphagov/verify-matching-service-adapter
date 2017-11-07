package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.messages.domain.MessagesImpl;
import org.beanplanet.validation.PredicatedValidator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.validators.exceptions.SamlResponseValidationException;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;

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