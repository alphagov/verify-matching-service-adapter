package uk.gov.ida.verifymatchingservicetesttool.resolvers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

public class FileUtilsResolver implements ParameterResolver {

  private static FileUtils fileUtils = null;

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
    return (parameterContext.getParameter().getType() == FileUtils.class);
	}

	@Override
	public FileUtils resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return fileUtils;
  }

  public static void setFileUtils(FileUtils newFileUtils) {
    fileUtils = newFileUtils;
  }
}