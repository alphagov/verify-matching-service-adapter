package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Application {

    public static void main(String[] args) {
        TestExecutionListener listener = new TestStatusPrintingListener();
        PackageSelector packageSelector = selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios");
        new Application().execute(
            listener,
            Arrays.asList(packageSelector),
            ConfigurationReader.getConfiguration()
        );
    }

    public void execute(
        TestExecutionListener listener,
        List<DiscoverySelector> selectors,
        ApplicationConfiguration applicationConfiguration
    ) {
        ApplicationConfigurationResolver.setConfiguration(applicationConfiguration);
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectors)
            .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
    }
}
