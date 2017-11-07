package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.saml.core.IdaSamlBootstrap;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationValidator.WRONG_SUBJECT_CONFIRMATION_METHOD;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;

@RunWith(MockitoJUnitRunner.class)
public class SubjectConfirmationValidatorTest {

    private SubjectConfirmationValidator<SubjectConfirmation> validator;

    @Mock
    public DateTimeComparator dateTimeComparator;

    @Before
    public void setup() {
        validator = new SubjectConfirmationValidator<>(identity(), dateTimeComparator);
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationMethodIsNotBearer() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withMethod("anything-but-not-bearer").build();

        Messages messages = validator.validate(subjectConfirmation, messages());

        assertThat(messages.hasErrorLike(WRONG_SUBJECT_CONFIRMATION_METHOD)).isTrue();
    }

}