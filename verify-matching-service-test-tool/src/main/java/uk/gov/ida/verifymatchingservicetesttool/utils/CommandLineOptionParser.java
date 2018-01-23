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
        if (exampleScenariosFolderLocation != null &&
            checkIfFileDoesNotExist(exampleScenariosFolderLocation, FolderName.MATCH_FOLDER_NAME) &&
            checkIfFileDoesNotExist(exampleScenariosFolderLocation, FolderName.NO_MATCH_FOLDER_NAME)) {
            System.out.println(String.format("%sTest Run Failed%s", Color.RED, Color.NONE));
            System.out.println(String.format("%sEnsure the specified example scenarios folder '%s' " +
                "has 'match' and 'no-match' sub-folders%s", Color.YELLOW, exampleScenariosFolderLocation, Color.NONE));
            System.exit(2);
        }

        String configFileLocation = getAbsoluteFilePath(cmd.getOptionValue(CONFIG_FILE.getValue()));
        if (configFileLocation != null && checkIfFileDoesNotExist(configFileLocation)) {
            System.out.println(String.format("%sTest Run Failed%s", Color.RED, Color.NONE));
            System.out.println(String.format("%sConfig file does not exist: '%s'%s", Color.YELLOW, configFileLocation, Color.NONE));
            System.exit(2);
        }

        return new CommandLineOptionValue(exampleScenariosFolderLocation, configFileLocation);
    }

    private static String getAbsoluteFilePath(String fileName) {
        if (fileName != null) {
            fileName = System.getProperty("user.dir") + File.separator + fileName;
        }
        return fileName;
    }

    private static boolean checkIfFileDoesNotExist(String fileName) {
        return !Files.exists(Paths.get(fileName));
    }

    private static boolean checkIfFileDoesNotExist(String fileName, FolderName folderName) {
        return !Files.exists(Paths.get(fileName, folderName.getValue()));
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
