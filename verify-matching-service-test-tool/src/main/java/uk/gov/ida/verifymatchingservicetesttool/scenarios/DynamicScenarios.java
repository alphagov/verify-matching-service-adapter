package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FileUtilsResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FilesLocatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.JsonValidatorResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.validators.JsonValidator;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.MATCH_FOLDER_NAME;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.NO_MATCH_FOLDER_NAME;

@ExtendWith(ApplicationConfigurationResolver.class)
@ExtendWith(FilesLocatorResolver.class)
@ExtendWith(JsonValidatorResolver.class)
@ExtendWith(FileUtilsResolver.class)
public class DynamicScenarios extends ScenarioBase {

    private FilesLocator filesLocator;
    private JsonValidator jsonValidator;

    public DynamicScenarios(
        ApplicationConfiguration configuration,
        FilesLocator filesLocator,
        JsonValidator jsonValidator,
        FileUtils fileUtils
    ) {
        super(configuration, fileUtils);
        this.filesLocator = filesLocator;
        this.jsonValidator = jsonValidator;
    }

    @TestFactory
    public Stream<DynamicTest> dynamicMatchTests() {
        return filesLocator.getFiles(MATCH_FOLDER_NAME)
            .map(file -> dynamicTest(file.getName(), getExecutable(file, true)));
    }

    @TestFactory
    public Stream<DynamicTest> dynamicNoMatchTests() {
        return filesLocator.getFiles(NO_MATCH_FOLDER_NAME)
            .map(file -> dynamicTest(file.getName(), getExecutable(file, false)));
    }

    private Executable getExecutable(File file, boolean isMatch) {
        return () -> {
            String jsonString = FileUtils.read(file);

            jsonValidator.validate(String.format("Invalid JSON in file '%s'.", file.getName()), jsonString);

            Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
                .request(APPLICATION_JSON)
                .post(Entity.json(jsonString));

            assertThat(response.getHeaderString("Content-Type"), is(APPLICATION_JSON));
            assertThat(response.getStatus(), is(OK.getStatusCode()));

            Map<String, String> result = readEntityAsMap(response);

            assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
            assertThat(result.get("result"), is(isMatch ? "match" : "no-match"));
        };
    }
}
