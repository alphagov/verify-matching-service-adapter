package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.CommandLineOptionValue;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FilesLocatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.CommandLineOptionParser;
import uk.gov.ida.verifymatchingservicetesttool.utils.ExitStatus;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.ScenarioFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Application {

    public static void main(String[] args) {
        CommandLineOptionValue commandLineConfig = CommandLineOptionParser.getConfigFileLocation(args);

        ExitStatus exitStatus = new Application().execute(
            new TestStatusPrintingListener(),
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            ConfigurationReader.getConfiguration(commandLineConfig.getConfigFileLocation()),
            new ScenarioFilesLocator(commandLineConfig.getExamplesFolderLocation()));

        System.exit(exitStatus.getExitCode());
    }

    public ExitStatus execute(SummaryGeneratingListener listener,
                              DiscoverySelector selector,
                              ApplicationConfiguration applicationConfiguration,
                              FilesLocator filesLocator) {
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
