package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FileUtilsResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FilesLocatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.JsonValidatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.ExitStatus;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Application {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: ./verify-matching-service-test-tool configuration.yml");
            System.exit(ExitStatus.ERROR.getExitCode());
        }

        ApplicationConfiguration configuration = null;
        try {
            configuration = new ConfigurationReader().getConfiguration(args[0]);
        } catch (MsaTestingToolConfigException e) {
            System.out.println("Unable to generate application configuration.");
            System.out.println(e.getMessage());
            System.out.println("Exiting system...");
            System.exit(ExitStatus.ERROR.getExitCode());
        }

        ExitStatus exitStatus = new Application().execute(
                new TestStatusPrintingListener(),
                selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
                configuration
        );

        System.exit(exitStatus.getExitCode());
    }

    public ExitStatus execute(SummaryGeneratingListener listener,
                              DiscoverySelector selector,
                              ApplicationConfiguration applicationConfiguration) {
        ApplicationConfigurationResolver.setConfiguration(applicationConfiguration);
        FilesLocatorResolver.setFilesLocator(applicationConfiguration.getFilesLocator());
        JsonValidatorResolver.setJsonValidator(applicationConfiguration.getJsonValidator());

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selector)
                .build();

        Collection<TestExecutionSummary> testSummaries = new HashSet<>();

        FileUtilsResolver.setFileUtils(applicationConfiguration.getFileUtils());
        testSummaries.add(executeTestRun(listener, request));

        return testSummaries.stream().anyMatch(summary -> !summary.getFailures().isEmpty())
                ? ExitStatus.FAILURE : ExitStatus.SUCCESS;
    }

    private TestExecutionSummary executeTestRun(SummaryGeneratingListener listener, LauncherDiscoveryRequest request) {
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        return listener.getSummary();
    }

}
