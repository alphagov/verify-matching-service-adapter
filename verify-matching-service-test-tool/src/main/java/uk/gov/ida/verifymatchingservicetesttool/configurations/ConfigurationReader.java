package uk.gov.ida.verifymatchingservicetesttool.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import uk.gov.ida.verifymatchingservicetesttool.Application;

import java.io.File;
import java.io.IOException;

public class ConfigurationReader {

    public static ApplicationConfiguration getConfiguration(String configFile) {
        try {
            return new ObjectMapper(new YAMLFactory()).readValue(
                new File(getConfigurationFolderLocation(configFile)),
                ApplicationConfiguration.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigurationFolderLocation(String configFile) {
        String path = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(path)
            .getParentFile()
            .getParentFile()
            .getAbsolutePath() + File.separator + configFile;
    }
}
