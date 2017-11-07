package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static uk.gov.ida.verifymatchingservicetesttool.utils.TestStatisticsFormatter.format;

public class Application {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"))
            .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        System.out.println(format(summary));
        System.out.println(format(summary.getFailures()));
    }
}
