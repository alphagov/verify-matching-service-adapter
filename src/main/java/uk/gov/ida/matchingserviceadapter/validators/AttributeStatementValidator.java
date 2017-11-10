package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.RequiredValidator;
import org.beanplanet.validation.Validator;
import org.joda.time.Duration;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.IdaConstants.Eidas_Attributes;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.Gender;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

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
                TimeRestrictionValidators.notInFutureValidator(new DateTimeComparator(Duration.ZERO), dob -> dob.getDateOfBirth().toDateTimeAtStartOfDay(), DATE_OF_BIRTH_IN_FUTURE)),
            optionalAttributeValidator(Eidas_Attributes.Gender.NAME, Gender.class, StringValidators.isOneOf(Gender::getValue, "Male", "Female", "Not Specified"))
        );
    }

    private static <R extends XMLObject> Validator<AttributeStatement> attributeValidator(String attributeName, Class<R> attributeClass, Validator<R> valueValidator) {
        return MatchingElementValidator.failOnMatchError((AttributeStatement as) -> as.getAttributes(),
            a -> a.getName().equals(attributeName),
            new AttributeValidator<>(identity(), attributeClass, valueValidator));
    }

    private static <R extends XMLObject> Validator<AttributeStatement> optionalAttributeValidator(String attributeName, Class<R> attributeClass, Validator<R> valueValidator) {
        return MatchingElementValidator.succeedOnMatchError((AttributeStatement as) -> as.getAttributes(),
            a -> a.getName().equals(attributeName),
            new AttributeValidator<>(identity(), attributeClass, valueValidator));
    }
}
