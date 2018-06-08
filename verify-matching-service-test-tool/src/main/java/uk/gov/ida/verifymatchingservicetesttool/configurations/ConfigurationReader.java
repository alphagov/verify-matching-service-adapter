package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;
import uk.gov.ida.verifymatchingservicetesttool.utils.Color;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ConfigurationReader {

    private static final String VERIFY_MATCHING_SERVICE_TEST_TOOL_YML = "verify-matching-service-test-tool.yml";

    public ApplicationConfiguration getConfiguration(String fileName) throws MsaTestingToolConfigException {
        File configFile = getAbsoluteFilePath(fileName);
        validateConfigFile(configFile);

        try {
            return new ObjectMapper(new YAMLFactory()).readValue(getConfigurationFolderLocation(configFile.getAbsolutePath()),
                    ApplicationConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getConfigurationFolderLocation(String configFileLocation) {
        String fileLocation = Optional.ofNullable(configFileLocation).orElseGet(this::getDefaultConfigLocation);
        return new File(fileLocation);
    }

    private String getDefaultConfigLocation() {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path)
                .getParentFile()
                .getParentFile()
                .getAbsolutePath() + File.separator + VERIFY_MATCHING_SERVICE_TEST_TOOL_YML;
    }

    private void validateConfigFile(File configFile) throws MsaTestingToolConfigException {
        if (!configFile.exists()) {
            throw new MsaTestingToolConfigException(String.format("%sConfig file does not exist: '%s'%s", Color.YELLOW, configFile.getName(), Color.NONE));
        }
    }

    File getAbsoluteFilePath(String fileName) {
        try {
            return new File(fileName);
        } catch (Exception ex) {
            return new File(System.getProperty("user.dir") + File.separator + fileName);
        }
    }
}
