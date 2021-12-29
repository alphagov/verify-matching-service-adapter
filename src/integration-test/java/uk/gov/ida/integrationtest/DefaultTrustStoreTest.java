package uk.gov.ida.integrationtest;

import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import java.security.KeyStoreException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTrustStoreTest {

    @RegisterExtension
    static MatchingServiceAdapterAppExtension prodMsaWithDefaultTruststoresApp = new MatchingServiceAdapterAppExtension(true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            ConfigOverride.config("metadata.environment", "PRODUCTION")
    );

    @RegisterExtension
    static MatchingServiceAdapterAppExtension integrationMsaWithDefaultTruststoresApp = new MatchingServiceAdapterAppExtension(
            true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            ConfigOverride.config("metadata.environment", "INTEGRATION")
    );

    @Test
    public void prodTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = prodMsaWithDefaultTruststoresApp
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow(
                        () -> new RuntimeException("No metadata configuration found")
                );
        List<String> hubAliases = Collections.list(metadataConfiguration
                .getHubTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No hub trust store found")
                ).aliases()
        );
        List<String> idpAliases = Collections.list(metadataConfiguration
                .getIdpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No IDP trust store found")
                ).aliases()
        );
        List<String> metadataAliases = Collections.list(metadataConfiguration
                .getTrustStore()
                .aliases()
        );

        assertThat(hubAliases).containsExactlyInAnyOrder(
                "root-ca",
                "hub-ca",
                "root-ca-g3",
                "core-ca-g3"
        );
        assertThat(idpAliases).containsExactlyInAnyOrder(
                "root-ca",
                "idp-ca",
                "root-ca-g3",
                "idp-ca-g3"
        );
        assertThat(metadataAliases).containsExactlyInAnyOrder(
                "root-ca",
                "metadata-ca",
                "core-ca-g2",
                "root-ca-g3",
                "metadata-ca-g3",
                "core-ca-g3"
        );
    }

    @Test
    public void integrationTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = integrationMsaWithDefaultTruststoresApp
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow(
                        () -> new RuntimeException("No metadata configuration found")
                );
        List<String> hubAliases = Collections.list(metadataConfiguration
                .getHubTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No hub trust store found")
                ).aliases()
        );
        List<String> idpAliases = Collections.list(metadataConfiguration
                .getIdpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No IDP trust store found")
                ).aliases()
        );
        List<String> metadataAliases = Collections.list(metadataConfiguration
                .getTrustStore()
                .aliases()
        );


        assertThat(hubAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "test-hub-old-ca",
                "test-hub-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-dev-pki-core-ca",
                "test-dev-pki-root-ca"
        );
        assertThat(idpAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "test-idp-ca",
                "test-root-ca-g3",
                "test-idp-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-intermediary-ca"
        );
        assertThat(metadataAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "core-test-ca",
                "test-ca",
                "metadata-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-idp-ca-g3",
                "test-metadata-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-core-ca",
                "test-dev-pki-intermediary-ca",
                "test-dev-pki-metadata-ca"
        );
    }
}
