package feature.uk.gov.ida.verifymatchingservicetesttool;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;
import uk.gov.ida.verifymatchingservicetesttool.utils.ExitStatus;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.NO_MATCH;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.RELATIVE_MATCH_URL;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class ApplicationScenariosExecutionFeatureTest extends FeatureTestBase {

    @Test
    public void shouldRunAllTestsForLegacyDataSchemaWhenUniversalDatasetFlagMissing() throws MsaTestingToolConfigException {
        localMatchingService.ensureDefaultMatchScenariosExist();

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
            .build();

        TestStatusPrintingListener listener = new TestStatusPrintingListener();
        application.execute(
            listener,
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            applicationConfiguration
        );

        assertThat(
            listener.getSummary().getTestsSucceededCount(),
            is(7L)
        );
    }

    @Test
    public void shouldRunAllTestsForUniversalDatasetWhenUniversalDatasetFlagIsTrue() throws MsaTestingToolConfigException {
        localMatchingService.ensureDefaultMatchScenariosExist();

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
                .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
                .withUsesUniversalDataSet(true)
                .build();

        TestStatusPrintingListener listener = new TestStatusPrintingListener();
        application.execute(
                listener,
                selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
                applicationConfiguration
        );

        assertThat(
                listener.getSummary().getTestsSucceededCount(),
                is(10L)
        );
    }

    @Test
    public void shouldReturnFailureExitCodeIfAnyScenariosFail() throws MsaTestingToolConfigException {

        localMatchingService.ensureResponseFor(
            RELATIVE_MATCH_URL,
            OK,
            String.format("{\"result\": \"%s\"}", NO_MATCH.getResult())
        );

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
            .build();

        ExitStatus exitStatus = application.execute(
            listener,
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            applicationConfiguration
        );

        assertThat(exitStatus, is(ExitStatus.FAILURE));
    }

    @Test
    public void shouldReturnSuccessExitCodeIfAllScenariosPass() throws MsaTestingToolConfigException {

        localMatchingService.ensureDefaultMatchScenariosExist();

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withLocalMatchingServiceAccountCreationUrl(localMatchingService.getAccountCreationUrl())
            .build();

        ExitStatus exitStatus = application.execute(
            listener,
            selectPackage("uk.gov.ida.verifymatchingservicetesttool.scenarios"),
            applicationConfiguration
        );

        assertThat(exitStatus, is(ExitStatus.SUCCESS));
    }
}
