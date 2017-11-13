package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@ExtendWith(ApplicationConfigurationResolver.class)
public abstract class ScenarioBase {

    protected FileUtils fileUtils = new FileUtils();
    protected Client client = ClientBuilder.newClient();
    protected ApplicationConfiguration configuration;

    public ScenarioBase(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }
}
