package feature.uk.gov.ida.verifymatchingservicetesttool;

import common.uk.gov.ida.verifymatchingservicetesttool.utils.TestsFilesLocator;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.DynamicScenarios;
import uk.gov.ida.verifymatchingservicetesttool.utils.FolderName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.MATCH_FOLDER_NAME;

public class JsonFilesValidationFeatureTest extends FeatureTestBase {

    @Test
    public void givenBadlyFormedJsonThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("malformed.json",
                false);
        application.execute(
            listener,
            selectClass(DynamicScenarios.class),
            applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
            firstFailure.getException().getMessage(),
            allOf(
                containsString(String.format("Invalid JSON in file '%s'. Reason: ", "malformed.json")),
                containsString("Unrecognized token")
            )
        );
    }

    @Test
    public void givenWellFormedLegacyJsonWithWrongSchemaThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("invalid-schema.json",
                false);

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ", "invalid-schema.json")),
                        containsString("required key [matchId] not found"),
                        containsString("required key [levelOfAssurance] not found"),
                        containsString("required key [hashedPid] not found"),
                        containsString("required key [matchingDataset] not found")
                )
        );
    }

    @Test
    public void givenWellFormedJsonWithWrongUniversalSchemaThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("invalid-schema.json",
                true);

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ", "invalid-schema.json")),
                        containsString("required key [matchId] not found"),
                        containsString("required key [levelOfAssurance] not found"),
                        containsString("required key [hashedPid] not found"),
                        containsString("required key [matchingDataset] not found")
                )
        );
    }

    @Test
    public void givenWellFormedUniversalJsonWithInvalidValuesThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("universal-invalid-values.json",
                true);

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ",
                                "universal-invalid-values.json")),
                        containsString("is not a valid date-time"),
                        containsString("is not a valid enum value")
                )
        );
    }

    @Test
    public void givenWellFormedUniversalJsonWithWrongWithMissingSchemaElementsThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("universal-missing-keys.json",
                true);

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ",
                                "universal-missing-keys.json")),
                        containsString("required key [firstName] not found"),
                        containsString("required key [surnames] not found"),
                        containsString("required key [dateOfBirth] not found")
                )
        );
    }

    @Test
    public void givenWellFormedUniversalJsonWithWrongKeysThenShowRelevantError() throws MsaTestingToolConfigException {
        ApplicationConfiguration applicationConfiguration = setupApplication("universal-invalid-keys.json",
                true);

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ",
                                "universal-invalid-keys.json")),
                        containsString("extraneous key [fromDate] is not permitted")
                )
        );
    }

    private ApplicationConfiguration setupApplication(String jsonFile, boolean usesUniversalDataset) throws MsaTestingToolConfigException {
        localMatchingService.ensureDefaultMatchScenariosExist();

        Map<FolderName, List<String>> testFiles = new HashMap<FolderName, List<String>>(){{
            put(MATCH_FOLDER_NAME, singletonList(jsonFile));
        }};

        return aApplicationConfiguration()
                .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withUsesUniversalDataSet(usesUniversalDataset)
                .withFilesLocator(new TestsFilesLocator(testFiles))
                .build();
    }
}
