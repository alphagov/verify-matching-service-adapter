package feature.uk.gov.ida.verifymatchingservicetesttool;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.utils.DefaultFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class ApplicationScenariosExecutionFeatureTest extends FeatureTestBase {

    @Test
    public void shouldRunAllTestsThatArePartOfTheApplication() {
        localMatchingService.ensureDefaultMatchScenariosExist();

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
            .build();

        listener = new TestStatusPrintingListener();
        application.execute(
            listener,
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            applicationConfiguration,
            fileLocator
        );

        assertThat(
            "There are failures. Check the printed result above.",
            listener.getSummary().getFailures().isEmpty(),
            is(true)
        );
    }
}