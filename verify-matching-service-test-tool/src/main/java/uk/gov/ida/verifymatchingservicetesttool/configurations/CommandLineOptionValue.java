package uk.gov.ida.verifymatchingservicetesttool.configurations;

import java.io.File;

public class CommandLineOptionValue {

    private String examplesFolderLocation;
    private String configFileLocation;

    public CommandLineOptionValue(String examplesFolderLocation, String configFileLocation) {
        this.examplesFolderLocation = examplesFolderLocation;
        this.configFileLocation = configFileLocation;
    }

    public String getExamplesFolderLocation() {
        return examplesFolderLocation != null ?
                System.getProperty("user.dir") + File.separator + examplesFolderLocation : null;
    }

    public String getConfigFileLocation() {
        return configFileLocation != null ?
                System.getProperty("user.dir") + File.separator + configFileLocation : null;
    }
}
