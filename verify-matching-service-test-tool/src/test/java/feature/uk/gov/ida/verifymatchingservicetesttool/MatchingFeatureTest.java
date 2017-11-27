package feature.uk.gov.ida.verifymatchingservicetesttool;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.DynamicScenarios;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.MATCH;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.NO_MATCH;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.RELATIVE_MATCH_URL;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class MatchingFeatureTest extends FeatureTestBase {

    @Test
    public void shouldFailWhenMatchExpectedButGotNoMatch() {
        localMatchingService.ensureResponseFor(
            RELATIVE_MATCH_URL,
            OK,
            String.format("{\"result\": \"%s\"}", NO_MATCH.getResult())
        );

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .build();

        application.execute(listener, selectClass(DynamicScenarios.class), applicationConfiguration, fileLocator);

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
            firstFailure.getException().getMessage(),
            allOf(
                containsString("Expected: is \"match\""),
                containsString("but: was \"no-match\"")
            )
        );
    }

    @Test
    public void shouldFailWhenNoMatchExpectedButGotMatch() {
        localMatchingService.ensureResponseFor(
            RELATIVE_MATCH_URL,
            OK,
            String.format("{\"result\": \"%s\"}", MATCH.getResult())
        );

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .build();

        application.execute(listener, selectClass(DynamicScenarios.class), applicationConfiguration, fileLocator);

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
            firstFailure.getException().getMessage(),
            allOf(
                containsString("Expected: is \"no-match\""),
                containsString("but: was \"match\"")
            )
        );
    }
}
