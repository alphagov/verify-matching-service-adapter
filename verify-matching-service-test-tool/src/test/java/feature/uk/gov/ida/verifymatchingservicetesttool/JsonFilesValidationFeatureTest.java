package feature.uk.gov.ida.verifymatchingservicetesttool;

import common.uk.gov.ida.verifymatchingservicetesttool.utils.TestsFilesLocator;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.DynamicScenarios;
import uk.gov.ida.verifymatchingservicetesttool.utils.FolderName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.uk.gov.ida.verifymatchingservicetesttool.builders.ApplicationConfigurationBuilder.aApplicationConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.MATCH_FOLDER_NAME;

public class JsonFilesValidationFeatureTest extends FeatureTestBase {

    @Test
    public void givenBadlyFormedJsonThenShowRelevantError() {
        localMatchingService.ensureDefaultMatchScenariosExist();

        Map<FolderName, List<String>> testFiles = new HashMap<FolderName, List<String>>(){{
            put(MATCH_FOLDER_NAME, Arrays.asList("badly-formed.json"));
        }};

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
            .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
            .withFilesLocator(new TestsFilesLocator(testFiles))
            .build();

        application.execute(
            listener,
            selectClass(DynamicScenarios.class),
            applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
            firstFailure.getException().getMessage(),
            allOf(
                containsString(String.format("Invalid JSON in file '%s'. Reason: ", "badly-formed.json")),
                containsString("Unrecognized token")
            )
        );
    }

    @Test
    public void givenWellFormedLegacyJsonWithWrongSchemaThenShowRelevantError() {
        localMatchingService.ensureDefaultMatchScenariosExist();

        Map<FolderName, List<String>> testFiles = new HashMap<FolderName, List<String>>() {{
            put(MATCH_FOLDER_NAME, Arrays.asList("well-formed-wrong-schema.json"));
        }};

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
                .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withFilesLocator(new TestsFilesLocator(testFiles))
                .build();

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ", "well-formed-wrong-schema.json")),
                        containsString("required key [matchId] not found"),
                        containsString("required key [levelOfAssurance] not found"),
                        containsString("required key [hashedPid] not found"),
                        containsString("required key [matchingDataset] not found")
                )
        );
    }

    @Test
    public void givenWellFormedUniversalJsonWithWrongSchemaThenShowRelevantError() {
        localMatchingService.ensureDefaultMatchScenariosExist();

        Map<FolderName, List<String>> testFiles = new HashMap<FolderName, List<String>>(){{
            put(MATCH_FOLDER_NAME, Arrays.asList("well-formed-wrong-schema.json"));
        }};

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
                .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withUsesUniversalDataSet(true)
                .withFilesLocator(new TestsFilesLocator(testFiles))
                .build();

        application.execute(
                listener,
                selectClass(DynamicScenarios.class),
                applicationConfiguration
        );

        Failure firstFailure = listener.getSummary().getFailures().get(0);

        assertThat(
                firstFailure.getException().getMessage(),
                allOf(
                        containsString(String.format("Invalid JSON in file '%s'. JSON schema validation failed. Reason: ", "well-formed-wrong-schema.json")),
                        containsString("required key [matchId] not found"),
                        containsString("required key [levelOfAssurance] not found"),
                        containsString("required key [hashedPid] not found"),
                        containsString("required key [matchingDataset] not found")
                )
        );
    }

    @Test
    public void givenWellFormedUniversalJsonWithWrongWithMissingSchemaElementsThenShowRelevantError() {
        localMatchingService.ensureDefaultMatchScenariosExist();

        Map<FolderName, List<String>> testFiles = new HashMap<FolderName, List<String>>(){{
            put(MATCH_FOLDER_NAME, Arrays.asList("well-formed-wrong-universal-schema.json"));
        }};

        ApplicationConfiguration applicationConfiguration = aApplicationConfiguration()
                .withLocalMatchingServiceMatchUrl(localMatchingService.getMatchingUrl())
                .withUsesUniversalDataSet(true)
                .withFilesLocator(new TestsFilesLocator(testFiles))
                .build();

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
                                "well-formed-wrong-universal-schema.json")),
                        containsString("required key [firstName] not found"),
                        containsString("required key [surnames] not found"),
                        containsString("required key [dateOfBirth] not found")
                )
        );
    }
}
