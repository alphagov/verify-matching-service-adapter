package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.utils.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class ConfigurationReader {

    private static final String VERIFY_MATCHING_SERVICE_TEST_TOOL_YML = "verify-matching-service-test-tool.yml";

    public static ApplicationConfiguration getConfiguration(String[] args) {
        String configFileLocation = getConfigFileLocation(args);

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

    private static String getConfigFileLocation(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ./verify-matching-service-test-tool configuration.yml");
            System.exit(1);
        }

        String configFileLocation = getAbsoluteFilePath(args[0]);
        if (configFileLocation != null && checkIfFileDoesNotExist(configFileLocation)) {
            System.out.println(String.format("%sTest Run Failed%s", Color.RED, Color.NONE));
            System.out.println(String.format("%sConfig file does not exist: '%s'%s", Color.YELLOW, configFileLocation, Color.NONE));
            System.exit(2);
        }

        return configFileLocation;
    }

    public static String getAbsoluteFilePath(String fileName) {
        try {
            File file = new File(fileName);
            return file.getAbsolutePath();
        } catch (Exception ex) {
            return System.getProperty("user.dir") + File.separator + fileName;
        }
    }

    private static boolean checkIfFileDoesNotExist(String fileName) {
        return !Files.exists(Paths.get(fileName));
    }
}
