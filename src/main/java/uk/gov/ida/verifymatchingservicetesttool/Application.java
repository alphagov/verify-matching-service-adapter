package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Application {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"))
            .build();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(new TestStatusPrintingListener());
        launcher.execute(request);
    }
}
