package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.checkers.UniversalDatasetOnlyChecker;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FileUtilsResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ExtendWith(ApplicationConfigurationResolver.class)
@ExtendWith(FileUtilsResolver.class)
@ExtendWith(UniversalDatasetOnlyChecker.class)
public class SpecialNameCaseScenario extends ScenarioBase {

    public SpecialNameCaseScenario(ApplicationConfiguration configuration, FileUtils fileUtils) {
        super(configuration, fileUtils);
    }

    @Test
    @DisplayName("Simple request with non-Latin name values and transliteration")
    public void runForSimpleCaseWithNonLatinNameValuesAndTransliteration() throws Exception {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
                .request(APPLICATION_JSON)
                .post(Entity.json(fileUtils.readFromResources("special-name-case-with-non-latin-name.json")));

        assertMatchNoMatch(response);
    }

    @Test
    @DisplayName("Simple request with non-ASCII Latin characters from ISO/IEC 8859-15")
    public void runForSimpleCaseWithNonASCIILatinCharacters() throws Exception {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
                .request(APPLICATION_JSON)
                .post(Entity.json(fileUtils.readFromResources("special-name-case.json")));

        assertMatchNoMatch(response);
    }
}

