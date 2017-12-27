package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.apache.commons.cli.*;
import uk.gov.ida.verifymatchingservicetesttool.configurations.CommandLineOptionValue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.ida.verifymatchingservicetesttool.configurations.CommandLineOption.CONFIG_FILE;
import static uk.gov.ida.verifymatchingservicetesttool.configurations.CommandLineOption.EXAMPLES_FOLDER;

public class CommandLineOptionParser {

    public static CommandLineOptionValue getConfigFileLocation(String[] args) {
        Options options = getOptions();
        CommandLine cmd = null;

        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Matching Service Test Tool executable", options);
            System.exit(1);
        }

        String exampleScenariosFolderLocation = getAbsoluteFilePath(cmd.getOptionValue(EXAMPLES_FOLDER.getValue()));
        if (exampleScenariosFolderLocation != null && checkIfFileDoesNotExists(exampleScenariosFolderLocation, FolderName.MATCH_FOLDER_NAME) && checkIfFileDoesNotExists(exampleScenariosFolderLocation, FolderName.NO_MATCH_FOLDER_NAME)) {
            System.out.println("Ensure the specified example scenarios folder '" + exampleScenariosFolderLocation + "' has 'match' and 'no-match' sub-folders ");
            System.exit(2);
        }

        String configFileLocation = getAbsoluteFilePath(cmd.getOptionValue(CONFIG_FILE.getValue()));
        if (configFileLocation != null && checkIfFileDoesNotExists(configFileLocation)) {
            System.out.println("Config file does not exist : " + configFileLocation);
            System.exit(2);
        }

        return new CommandLineOptionValue(exampleScenariosFolderLocation, configFileLocation);
    }

    private static String getAbsoluteFilePath(String fileName) {
        return fileName != null ? System.getProperty("user.dir") + File.separator + fileName : fileName;
    }

    private static boolean checkIfFileDoesNotExists(String fileName) {
        return !Files.exists(Paths.get(fileName));
    }

    private static boolean checkIfFileDoesNotExists(String fileName, FolderName folderName) {
        return !Files.exists(Paths.get(fileName + File.separator + folderName.getValue()));
    }

    private static Options getOptions() {
        Option cliConfigFile = new Option(CONFIG_FILE.getValue(), "configFile",
            true, "location of test tool's config file");
        Option cliExamplesFolder = new Option(EXAMPLES_FOLDER.getValue(), "examplesFolder",
            true, "location of the example scenarios folder used by the test tool");

        cliConfigFile.setRequired(false);
        cliExamplesFolder.setRequired(false);

        Options options = new Options();
        options.addOption(cliConfigFile);
        options.addOption(cliExamplesFolder);
        return options;
    }

}
