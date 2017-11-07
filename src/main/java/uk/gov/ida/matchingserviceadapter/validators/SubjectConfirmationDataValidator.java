package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.RequiredValidator;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import java.util.function.Function;

public class SubjectConfirmationDataValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl CONFIRMATION_DATA_NOT_PRESENT = MessageImpl.globalMessage("confirmationData.absent", "confirmation data must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.onOrAfter", "confirmationData.onOrAfter.absent", "Not on or after must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_INVALID = MessageImpl.fieldMessage("confirmationData.notOnOrAfter", "confirmationData.notOnOrAfter.invalid", "Not on or after must not be in the past");
    public static final MessageImpl IN_RESPONSE_TO_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.inResponseTo", "confirmationData.inResponseTo.absent", "In response to must not be null");
    public static final MessageImpl RECIPIENT_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.recipient", "confirmationData.recipient.absent", "Recipient must not be null");

    public SubjectConfirmationDataValidator(Function<T, SubjectConfirmationData> valueProvider, DateTimeComparator dateTimeComparator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(CONFIRMATION_DATA_NOT_PRESENT),
            new RequiredValidator<>(NOT_ON_OR_AFTER_NOT_PRESENT, SubjectConfirmationData::getNotOnOrAfter),
            TimeRestrictionValidators.notInPastValidator(dateTimeComparator,  SubjectConfirmationData::getNotOnOrAfter, NOT_ON_OR_AFTER_INVALID),
            new RequiredValidator<>(IN_RESPONSE_TO_NOT_PRESENT, SubjectConfirmationData::getInResponseTo),
            new RequiredValidator<>(RECIPIENT_NOT_PRESENT, SubjectConfirmationData::getRecipient)
        );
    }

}
