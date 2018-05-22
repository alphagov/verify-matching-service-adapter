package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;
import uk.gov.ida.validation.validators.Validator;
import uk.gov.ida.saml.core.extensions.eidas.TransliterableString;

import java.util.function.Function;

import static java.util.function.Function.identity;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class AttributeValidator<T, R extends XMLObject> extends CompositeValidator<T> {

    public static final MessageImpl ATTRIBUTE_NOT_PRESENT = globalMessage("attribute", "Attribute not present");
    public static final MessageImpl ZERO_ATTRIBUTE_VALUES = MessageImpl.fieldMessage("attribute.attributeValues", "attribute.attributeValues.zero", "Zero attribute values");
    public static final MessageImpl TOO_MANY_ATTRIBUTE_VALUES = MessageImpl.fieldMessage("attribute.attributeValues", "attribute.attributeValues.too.many", "Too many attribute values");

    public AttributeValidator(Function<T, Attribute> valueProvider, Class<R> clazz, Validator<R> valueValidator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(ATTRIBUTE_NOT_PRESENT),
            new FixedErrorValidator<>(a -> a.getAttributeValues().size() == 0, ZERO_ATTRIBUTE_VALUES),
            (TransliterableString.class.isAssignableFrom(clazz) ?
                // The attribute is transliterable, hence it may either have a Latin and non-Latin value or just a Latin value.
                new CompositeValidator<>(
                    true,
                    new FixedErrorValidator<>(a -> a.getAttributeValues().size() > 2, TOO_MANY_ATTRIBUTE_VALUES),
                    MatchingElementValidator.succeedOnMatchError(Attribute::getAttributeValues, a-> !((TransliterableString)a).isLatinScript(),
                        new AttributeValueValidator<>(identity(), clazz, valueValidator)),
                    MatchingElementValidator.failOnMatchError(Attribute::getAttributeValues, a -> ((TransliterableString)a).isLatinScript(),
                        new AttributeValueValidator<>(identity(), clazz, valueValidator)))
                // The attribute is not transliterable, so there must be exactly one.
              : new CompositeValidator<>(
                  true,
                  new FixedErrorValidator<>(a -> a.getAttributeValues().size() != 1, TOO_MANY_ATTRIBUTE_VALUES),
                  new AttributeValueValidator<>(a -> a.getAttributeValues().get(0), clazz, valueValidator)))
        );
    }

    public static class AttributeValueValidator<T, R extends XMLObject> extends CompositeValidator<T> {
        public AttributeValueValidator(Function<T, XMLObject> valueProvider, Class<R> clazz, Validator<R> valueValidator) {
            super(
                true,
                valueProvider,
                TypeValidators.isValidatedInstanceOf(clazz, valueValidator)
            );
        }

    }

}
