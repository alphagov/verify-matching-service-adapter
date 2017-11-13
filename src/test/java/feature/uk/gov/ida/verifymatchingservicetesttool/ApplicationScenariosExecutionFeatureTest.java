package feature.uk.gov.ida.verifymatchingservicetesttool;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;

import java.util.Arrays;

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

        Application application = new Application();

        application.execute(
            listener,
            Arrays.asList(
                selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
                selectPackage("common.uk.gov.ida.verifymatchingservicetesttool.scenarios")
            ),
            applicationConfiguration
        );

        assertThat(
            "There are failures. Check the printed result above.",
            listener.getSummary().getFailures().isEmpty(),
            is(true)
        );
    }
}