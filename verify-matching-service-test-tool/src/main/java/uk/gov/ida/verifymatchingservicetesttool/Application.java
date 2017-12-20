package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FilesLocatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.DefaultFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.ExitStatus;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import org.apache.commons.cli.*;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Application {

    private static String examplesDirectory;
    private static String configFile;

    public static void main(String[] args) {

        Options options = new Options();

        Option cliExamplesDirectory = new Option("d","examplesDirectory",true, "location of json scenarios");
        Option cliConfigFile = new Option("c", "configFile", true, "location of config file");

        cliExamplesDirectory.setRequired(false);
        cliConfigFile.setRequired(false);

        options.addOption(cliExamplesDirectory);
        options.addOption(cliConfigFile);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Verify Matching Service Test Tool", options);

            System.exit(1);
            return;
        }

        if (cmd.hasOption("d")) {
            examplesDirectory = cmd.getOptionValue("examplesDirectory");
            System.out.println(String.format("location of matching scenarios defined as: %s", examplesDirectory));
        } else {
            System.out.println("using default location of matching scenarios: ./examples");
            examplesDirectory = "examples";
        }

        if (cmd.hasOption("c")) {
            configFile = cmd.getOptionValue("c");
            System.out.println(String.format("location of test tool config file: %s", configFile));
        } else {
            configFile = "verify-matching-service-test-tool.yml";
            System.out.println(String.format("using default test tool config file"));
        }

        ExitStatus exitStatus = new Application().execute(
            new TestStatusPrintingListener(),
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            ConfigurationReader.getConfiguration(configFile),
            new DefaultFilesLocator(examplesDirectory)
        );
        System.exit(exitStatus.getExitCode());
    }

    public ExitStatus execute(
        SummaryGeneratingListener listener,
        DiscoverySelector selector,
        ApplicationConfiguration applicationConfiguration,
        FilesLocator filesLocator
    ) {
        ApplicationConfigurationResolver.setConfiguration(applicationConfiguration);
        FilesLocatorResolver.setFilesLocator(filesLocator);

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selector)
            .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        return listener.getSummary().getFailures().isEmpty() ? ExitStatus.SUCCESS : ExitStatus.FAILURE;
    }
}
