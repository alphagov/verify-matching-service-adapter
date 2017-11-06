package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.FixedErrorValidator;
import org.opensaml.saml.saml2.core.SubjectConfirmation;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;
import static org.opensaml.saml.saml2.core.SubjectConfirmation.METHOD_BEARER;

public class SubjectConfirmationValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl WRONG_SUBJECT_CONFIRMATION_METHOD = fieldMessage("subjectConfirmations.method", "subjectConfirmations.method.incorrect", "Subject confirmation has wrong method");

    public SubjectConfirmationValidator(Function<T, SubjectConfirmation> valueProvider, TimeRestrictionValidator timeRestrictionValidator) {
        super(
            true,
            valueProvider,
            new FixedErrorValidator<>(sc -> !sc.getMethod().equals(METHOD_BEARER),  WRONG_SUBJECT_CONFIRMATION_METHOD),
            new SubjectConfirmationDataValidator<>(SubjectConfirmation::getSubjectConfirmationData, timeRestrictionValidator)
        );
    }

}
