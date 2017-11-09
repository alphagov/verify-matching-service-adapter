package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.Duration;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.IdaConstants.Eidas_Attributes;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.RequiredValidator;
import uk.gov.ida.validation.validators.Validator;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class AttributeStatementValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl ATTRIBUTE_STATEMENT_NOT_PRESENT = globalMessage("attributeStatements", "Attribute statements not present");
    public static final MessageImpl DATE_OF_BIRTH_IN_FUTURE = globalMessage("dob", "Date of birth may not be in the future");

    public AttributeStatementValidator(Function<T, AttributeStatement> valueProvider) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(ATTRIBUTE_STATEMENT_NOT_PRESENT),
            attributeValidator(Eidas_Attributes.FirstName.NAME, CurrentGivenName.class, StringValidators.isNonEmpty(CurrentGivenName::getFirstName)),
            attributeValidator(Eidas_Attributes.FamilyName.NAME, CurrentFamilyName.class, StringValidators.isNonEmpty(CurrentFamilyName::getFamilyName)),
            attributeValidator(Eidas_Attributes.PersonIdentifier.NAME, PersonIdentifier.class, StringValidators.isNonEmpty(PersonIdentifier::getPersonIdentifier)),
            attributeValidator(Eidas_Attributes.DateOfBirth.NAME,
                DateOfBirth.class,
                TimeRestrictionValidators.notInFutureValidator(new DateTimeComparator(Duration.ZERO), dob -> dob.getDateOfBirth().toDateTimeAtStartOfDay(), DATE_OF_BIRTH_IN_FUTURE))
        );
    }

    private static <R extends XMLObject> Validator<AttributeStatement> attributeValidator(String attributeName, Class<R> attributeClass, Validator<R> valueValidator) {
        return new MatchingElementValidator<>(
            (AttributeStatement as) -> as.getAttributes(),
            a -> a.getName().equals(attributeName),
            new AttributeValidator<>(identity(), attributeClass, valueValidator));
    }
}
