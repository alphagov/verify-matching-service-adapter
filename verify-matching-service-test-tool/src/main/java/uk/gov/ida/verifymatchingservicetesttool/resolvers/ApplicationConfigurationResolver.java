package uk.gov.ida.verifymatchingservicetesttool.resolvers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;

public class ApplicationConfigurationResolver implements ParameterResolver {

    private static ApplicationConfiguration configuration = null;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == ApplicationConfiguration.class);
    }

    @Override
    public ApplicationConfiguration resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return getConfiguration();
    }

    private ApplicationConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = ConfigurationReader.getConfiguration("verify-matching-service-test-tool.yml");
        }

        return configuration;
    }

    public static void setConfiguration(ApplicationConfiguration applicationConfiguration) {
        configuration = applicationConfiguration;
    }
}
