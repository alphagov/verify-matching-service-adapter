package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;

import java.util.function.Function;

import static org.opensaml.saml.saml2.core.SubjectConfirmation.METHOD_BEARER;
import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;

public class SubjectConfirmationValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl WRONG_SUBJECT_CONFIRMATION_METHOD = fieldMessage("subjectConfirmations.method", "subjectConfirmations.method.incorrect", "Subject confirmation has wrong method");

    public SubjectConfirmationValidator(Function<T, SubjectConfirmation> valueProvider, DateTimeComparator dateTimeComparator) {
        super(
            true,
            valueProvider,
            new FixedErrorValidator<>(sc -> !sc.getMethod().equals(METHOD_BEARER),  WRONG_SUBJECT_CONFIRMATION_METHOD),
            new SubjectConfirmationDataValidator<>(SubjectConfirmation::getSubjectConfirmationData, dateTimeComparator)
        );
    }

}
