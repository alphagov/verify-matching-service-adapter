package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

public class SubjectConfirmationDataValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl SUBJECT_CONFIRMATION_DATA_NOT_PRESENT = MessageImpl.globalMessage("confirmationData.absent", "Confirmation data must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.onOrAfter", "confirmationData.onOrAfter.absent", "Not on or after must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_INVALID = MessageImpl.fieldMessage("confirmationData.notOnOrAfter", "confirmationData.notOnOrAfter.invalid", "Not on or after must not be in the past");
    public static final MessageImpl IN_RESPONSE_TO_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.inResponseTo", "confirmationData.inResponseTo.absent", "In response to must not be null");
    public static final MessageImpl RECIPIENT_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.recipient", "confirmationData.recipient.absent", "Recipient must not be null");
    public static final MessageImpl RECIPIENT_INVALID = MessageImpl.fieldMessage("confirmationData.recipient", "confirmationData.recipient.invalid", "Recipient is not valid");

    public SubjectConfirmationDataValidator(Function<T, SubjectConfirmationData> valueProvider, DateTimeComparator dateTimeComparator, String hubConnectorEntityId) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(SUBJECT_CONFIRMATION_DATA_NOT_PRESENT),
            new RequiredValidator<>(NOT_ON_OR_AFTER_NOT_PRESENT, SubjectConfirmationData::getNotOnOrAfter),
            TimeRestrictionValidators.notInPastValidator(dateTimeComparator,  SubjectConfirmationData::getNotOnOrAfter, NOT_ON_OR_AFTER_INVALID),
            new RequiredValidator<>(IN_RESPONSE_TO_NOT_PRESENT, SubjectConfirmationData::getInResponseTo),
            new RequiredValidator<>(RECIPIENT_NOT_PRESENT, SubjectConfirmationData::getRecipient),
            new FixedErrorValidator<>(subjectConfData -> !subjectConfData.getRecipient().equals(hubConnectorEntityId), RECIPIENT_INVALID)
        );
    }

}
