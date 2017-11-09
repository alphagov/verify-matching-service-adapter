package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.FixedErrorValidator;
import org.beanplanet.validation.RequiredValidator;
import org.beanplanet.validation.Validator;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

public class AttributeValidator<T, R extends XMLObject> extends CompositeValidator<T> {

    public static final MessageImpl ATTRIBUTE_NOT_PRESENT = globalMessage("attribute", "Attribute not present");
    public static final MessageImpl WRONG_NUMBER_OF_ATTRIBUTE_VALUES = MessageImpl.fieldMessage("attribute.attributeValues", "attribute.attributeValues.wrong.size", "Wrong number of attribute values");

    public AttributeValidator(Function<T, Attribute> valueProvider, Class<R> clazz, Validator<R> valueValidator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(ATTRIBUTE_NOT_PRESENT),
            new FixedErrorValidator<>(a -> a.getAttributeValues().size() != 1, WRONG_NUMBER_OF_ATTRIBUTE_VALUES),
            new AttributeValueValidator<>(a -> a.getAttributeValues().get(0), clazz, valueValidator)
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
