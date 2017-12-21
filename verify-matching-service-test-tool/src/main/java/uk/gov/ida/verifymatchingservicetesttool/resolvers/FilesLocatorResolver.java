package uk.gov.ida.verifymatchingservicetesttool.resolvers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.ScenarioFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;

public class FilesLocatorResolver implements ParameterResolver {

    private static FilesLocator applicationFilesLocator = null;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == FilesLocator.class);
    }

    @Override
    public FilesLocator resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        if (applicationFilesLocator == null) {
            applicationFilesLocator = new ScenarioFilesLocator("examples");
        }

        return applicationFilesLocator;
    }

    public static void setFilesLocator(FilesLocator filesLocator) {
        applicationFilesLocator = filesLocator;
    }
}
