package uk.gov.ida.matchingserviceadapter.validators;

import com.google.common.collect.ImmutableSet;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.Set;
import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class SubjectValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl SUBJECT_NOT_PRESENT = globalMessage("subject", "Subject not present");
    public static final MessageImpl WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS = fieldMessage("subjectConfirmations", "subject.subjectConfirmations.wrong.size", "Must have exactly 1 subject confirmation");
    public static final MessageImpl NAME_ID_NOT_PRESENT = fieldMessage("subject.nameId", "subject.nameId.absent", "NameID not present");
    public static final MessageImpl NAME_ID_IN_WRONG_FORMAT = fieldMessage("subject.nameId", "subject.nameId.wrong.format", "NameID not in valid format");

    public static final Set<String> VALID_IDENTIFIERS = ImmutableSet.of(NameID.PERSISTENT);

    public SubjectValidator(Function<T, Subject> valueProvider, TimeRestrictionValidator timeRestrictionValidator, String requestId) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(SUBJECT_NOT_PRESENT),
            new FixedErrorValidator<>(subject -> subject.getSubjectConfirmations().size() != 1, WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS),
            new RequiredValidator<>(NAME_ID_NOT_PRESENT, Subject::getNameID),
            new FixedErrorValidator<>(subject -> !VALID_IDENTIFIERS.contains(subject.getNameID().getFormat()), NAME_ID_IN_WRONG_FORMAT),
            new SubjectConfirmationValidator<>(subject -> subject.getSubjectConfirmations().get(0), timeRestrictionValidator, requestId)
        );
    }

}
