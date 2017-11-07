package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.NameIdBuilder;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.CONFIRMATION_DATA_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationValidator.WRONG_SUBJECT_CONFIRMATION_METHOD;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.NAME_ID_IN_WRONG_FORMAT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.NAME_ID_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(OpenSAMLMockitoRunner.class)
public class SubjectValidatorTest {
    private SubjectValidator<Subject> subjectValidator;

    @Before
    public void setUp() {
        subjectValidator = new SubjectValidator(identity(), new DateTimeComparator(Duration.ZERO));
    }

    @Test
    public void shouldGenerateNoErrorsWhenSubjectIsValid() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectIsMissing() throws Exception {
        Messages messages = subjectValidator.validate(null, messages());

        assertThat(messages.hasErrorLike(SUBJECT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenNameIdIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .withNameId(null)
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(NAME_ID_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenNameIdFormatIsIncorrect() throws Exception {
        String incorrectFormat = "An incorrect format";

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .withNameId(NameIdBuilder.aNameId().withFormat(incorrectFormat).build())
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(NAME_ID_IN_WRONG_FORMAT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenMultipleSubjectConfirmations() throws Exception {
        Subject subject = aSubject()
            .withSubjectConfirmation(aSubjectConfirmation().build())
            .withSubjectConfirmation(aSubjectConfirmation().build())
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS)).isTrue();
    }

    @Test
    public void shouldValidateSubjectConfirmation() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withMethod("anything-but-not-bearer").build())
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(WRONG_SUBJECT_CONFIRMATION_METHOD)).isTrue();
    }

    @Test
    public void shouldValidateSubjectConfirmationData() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(CONFIRMATION_DATA_NOT_PRESENT)).isTrue();
    }

}