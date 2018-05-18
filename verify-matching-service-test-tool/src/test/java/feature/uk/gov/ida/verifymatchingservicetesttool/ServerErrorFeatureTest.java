package feature.uk.gov.ida.verifymatchingservicetesttool;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.LevelOfAssuranceOneScenario;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.RELATIVE_MATCH_URL;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class ServerErrorFeatureTest extends FeatureTestBase {

    @Test
    public void shouldFailWhenInternalServerError() {
        localMatchingService.ensureResponseFor(RELATIVE_MATCH_URL, INTERNAL_SERVER_ERROR, null);

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withUsesUniversalDataSet(true)
            .build();

        application.execute(
            listener,
            selectClass(LevelOfAssuranceOneScenario.class),
            applicationConfiguration,
            fileLocator
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
            firstFailure.getException().getMessage(),
            allOf(
                containsString("Expected: is <200>"),
                containsString("but: was <500>")
            )
        );
    }
}
