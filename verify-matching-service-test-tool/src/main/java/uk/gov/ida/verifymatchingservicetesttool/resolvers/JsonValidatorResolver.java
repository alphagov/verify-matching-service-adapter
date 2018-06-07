package uk.gov.ida.verifymatchingservicetesttool.resolvers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import uk.gov.ida.verifymatchingservicetesttool.validators.JsonValidator;

public class JsonValidatorResolver implements ParameterResolver {

    private static JsonValidator jsonValidator = null;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == JsonValidator.class);
    }

    @Override
    public JsonValidator resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (jsonValidator == null) {
            jsonValidator = new JsonValidator("legacy");
        }

        return jsonValidator;
    }

    public static void setJsonValidator(JsonValidator validator) {
        jsonValidator = validator;
    }
}
