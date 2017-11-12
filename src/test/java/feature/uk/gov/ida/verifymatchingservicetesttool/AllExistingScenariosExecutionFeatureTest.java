package feature.uk.gov.ida.verifymatchingservicetesttool;

import common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.PackageSelector;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import java.util.Arrays;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class AllExistingScenariosExecutionFeatureTest {

    private LocalMatchingServiceStub localMatchingService = new LocalMatchingServiceStub();
    private TestStatusPrintingListener listener = new TestStatusPrintingListener();

    @BeforeEach
    public void setUp() {
        localMatchingService.start();
    }

    @AfterEach
    public void tearDown() {
        localMatchingService.stop();
    }

    @Test
    public void shouldRunAllTestsThatArePartOfTheApplication() {
        localMatchingService.ensureDefaultMatchScenariosExists();

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
            .build();

        Application application = new Application() {{
            setApplicationConfiguration(applicationConfiguration);
        }};

        PackageSelector packageSelector = selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios");
        application.execute(listener, Arrays.asList(packageSelector));

        assertThat("There are failures. Check the printed result above.", listener.hasFailures(), is(false));
    }
}