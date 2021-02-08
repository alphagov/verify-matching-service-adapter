package uk.gov.ida.integrationtest;

import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;

public class VerifyUserAccountCreationIntegrationTest extends UserAccountCreationBaseTest {
    private static final boolean COUNTRY_ENABLED_FALSE = false;

    @ClassRule
    public static final HttpStubRule localMatchingService = new HttpStubRule();
    @ClassRule
    public static final MatchingServiceAdapterAppRule applicationRule = new MatchingServiceAdapterAppRule(
        COUNTRY_ENABLED_FALSE,
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH),
        ConfigOverride.config("localMatchingService.accountCreationUrl", "http://localhost:" + localMatchingService.getPort() + UNKNOWN_USER_MATCHING_PATH)
    );

    @Override
    protected MatchingServiceAdapterAppRule getAppRule() {
        return applicationRule;
    }

    @Override
    protected HttpStubRule setUpMatchingService() throws Exception {
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"success\"}");
        return localMatchingService;
    }

}
