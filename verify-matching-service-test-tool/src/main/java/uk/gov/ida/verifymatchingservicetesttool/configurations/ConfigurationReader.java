package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uk.gov.ida.verifymatchingservicetesttool.Application;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ConfigurationReader {

    private static final String VERIFY_MATCHING_SERVICE_TEST_TOOL_YML = "verify-matching-service-test-tool.yml";

    public static ApplicationConfiguration getConfiguration(String configFileLocation) {
        try {
            return new ObjectMapper(new YAMLFactory()).readValue(new File(getConfigurationFolderLocation(configFileLocation)),
                    ApplicationConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigurationFolderLocation(String configFileLocation) {
        return Optional.ofNullable(configFileLocation).orElseGet(ConfigurationReader::getDefaultConfigLocation);
    }

    private static String getDefaultConfigLocation() {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path)
                .getParentFile()
                .getParentFile()
                .getAbsolutePath() + File.separator + VERIFY_MATCHING_SERVICE_TEST_TOOL_YML;
    }
}
