package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.DynamicScenariosFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.MATCH_FOLDER_NAME;
import static uk.gov.ida.verifymatchingservicetesttool.utils.FolderName.NO_MATCH_FOLDER_NAME;

@ExtendWith(ApplicationConfigurationResolver.class)
public class DynamicScenarios extends ScenarioBase {

    private DynamicScenariosFilesLocator filesLocator = new DynamicScenariosFilesLocator();

    public DynamicScenarios(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @TestFactory
    public Stream<DynamicTest> dynamicMatchTests() throws Exception {
        return filesLocator.getFiles(MATCH_FOLDER_NAME)
            .map(file -> dynamicTest(file.getName(), getExecutable(file, true)));
    }

    @TestFactory
    public Stream<DynamicTest> dynamicNoMatchTests() throws Exception {
        return filesLocator.getFiles(NO_MATCH_FOLDER_NAME)
            .map(file -> dynamicTest(file.getName(), getExecutable(file, false)));
    }

    private Executable getExecutable(File file, boolean isMatch) {
        return () -> {
            Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
                .request(APPLICATION_JSON)
                .post(Entity.json(fileUtils.read(file)));

            Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

            assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
            assertThat(result.get("result"), is(isMatch ? "match" : "no-match"));
        };
    }
}
