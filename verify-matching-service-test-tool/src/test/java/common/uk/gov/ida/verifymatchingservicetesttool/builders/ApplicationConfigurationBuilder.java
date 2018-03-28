package common.uk.gov.ida.verifymatchingservicetesttool.builders;

import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.LocalMatchingServiceConfiguration;

import java.net.URI;

public class ApplicationConfigurationBuilder {

    private URI localMatchingServiceMatchUrl = URI.create("http://example.com");
    private URI localMatchingServiceAccountCreationUrl = URI.create("http://example.com");
    private Boolean usesUniversalDataSet = null;

    public static ApplicationConfigurationBuilder aApplicationConfiguration() {
        return new ApplicationConfigurationBuilder();
    }

    public ApplicationConfiguration build() {
        LocalMatchingServiceConfiguration localMatchingServiceConfiguration = new LocalMatchingServiceConfiguration(
            localMatchingServiceMatchUrl,
            localMatchingServiceAccountCreationUrl,
            usesUniversalDataSet
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

    public ApplicationConfigurationBuilder withUsesUniversalDataSet(Boolean value) {
        this.usesUniversalDataSet = value;
        return this;
    }
}
