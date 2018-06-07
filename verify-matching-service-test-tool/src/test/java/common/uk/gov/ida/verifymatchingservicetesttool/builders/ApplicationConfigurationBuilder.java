package common.uk.gov.ida.verifymatchingservicetesttool.builders;

import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.configurations.LocalMatchingServiceConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;

import java.net.URI;
import java.util.Optional;

public class ApplicationConfigurationBuilder {

    private URI localMatchingServiceMatchUrl = URI.create("http://example.com");
    private URI localMatchingServiceAccountCreationUrl = URI.create("http://example.com");
    private boolean usesUniversalDataSet;
    private String examplesFolderLocation = null;
    private FilesLocator filesLocator = null;

    public static ApplicationConfigurationBuilder aApplicationConfiguration() {
        return new ApplicationConfigurationBuilder();
    }

    public ApplicationConfiguration build() {
        LocalMatchingServiceConfiguration localMatchingServiceConfiguration = new LocalMatchingServiceConfiguration(
            localMatchingServiceMatchUrl,
            localMatchingServiceAccountCreationUrl,
            usesUniversalDataSet
        );

        return new TestApplicationConfiguration(localMatchingServiceConfiguration, examplesFolderLocation, filesLocator);
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

    public ApplicationConfigurationBuilder withFilesLocator(FilesLocator locator) {
        this.filesLocator = locator;
        return this;
    }

    private class TestApplicationConfiguration extends ApplicationConfiguration {
        private final FilesLocator filesLocator;

        public TestApplicationConfiguration(LocalMatchingServiceConfiguration localMatchingService,
                                            String examplesFolderLocation,
                                            FilesLocator filesLocator) {
            super(localMatchingService, examplesFolderLocation);
            this.filesLocator = filesLocator;
        }

        @Override
        public FilesLocator getFilesLocator() {
            return Optional.ofNullable(filesLocator).orElseGet(super::getFilesLocator);
        }
    }
}
