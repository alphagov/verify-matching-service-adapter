package common.uk.gov.ida.verifymatchingservicetesttool.builders;

import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.LocalMatchingServiceConfiguration;

import java.net.URI;

public class ApplicationConfigurationBuilder {

    private URI localMatchingServiceMatchUrl = URI.create("http://example.com");
    private URI localMatchingServiceAccountCreationUrl = URI.create("http://example.com");

    public static ApplicationConfigurationBuilder aApplicationConfiguration() {
        return new ApplicationConfigurationBuilder();
    }

    public ApplicationConfiguration build() {
        LocalMatchingServiceConfiguration localMatchingServiceConfiguration = new LocalMatchingServiceConfiguration(
            localMatchingServiceMatchUrl,
            localMatchingServiceAccountCreationUrl
        );

        return new ApplicationConfiguration(localMatchingServiceConfiguration);
    }

    public ApplicationConfigurationBuilder withLocalMatchingServiceMatchUrl(URI localMatchingServiceMatchUrl) {
        this.localMatchingServiceMatchUrl = localMatchingServiceMatchUrl;
        return this;
    }

    public ApplicationConfigurationBuilder withLocalMatchingServiceAccountCreationUrl(URI localMatchingServiceAccountCreationUrl) {
        this.localMatchingServiceAccountCreationUrl = localMatchingServiceAccountCreationUrl;
        return this;
    }
}
