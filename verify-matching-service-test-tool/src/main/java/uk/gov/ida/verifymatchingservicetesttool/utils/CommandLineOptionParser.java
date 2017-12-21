package uk.gov.ida.verifymatchingservicetesttool.utils;

import org.apache.commons.cli.*;
import uk.gov.ida.verifymatchingservicetesttool.configurations.CommandLineOptionValue;

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
            new HelpFormatter().printHelp("Verify Matching Service Test Tool", options);
            System.exit(1);
        }
        return new CommandLineOptionValue(cmd.getOptionValue(EXAMPLES_FOLDER.getValue()),
            cmd.getOptionValue(CONFIG_FILE.getValue()));
    }

    private static Options getOptions() {
        Option cliConfigFile = new Option(CONFIG_FILE.getValue(), "configFile",
            true, "location of test tool's config file");
        Option cliExamplesFolder = new Option(EXAMPLES_FOLDER.getValue(), "examplesFolder",
            true, "location of the example scenarios used by the test tool");

        cliConfigFile.setRequired(false);
        cliExamplesFolder.setRequired(false);

        Options options = new Options();
        options.addOption(cliConfigFile);
        options.addOption(cliExamplesFolder);
        return options;
    }

}
