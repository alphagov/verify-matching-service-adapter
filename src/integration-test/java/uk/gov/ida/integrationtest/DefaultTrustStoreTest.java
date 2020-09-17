package uk.gov.ida.integrationtest;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTrustStoreTest {

    @ClassRule
    public static final MatchingServiceAdapterAppRule prodMsaWithDefaultTruststoresApplicationRule = new MatchingServiceAdapterAppRule(
            true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            ConfigOverride.config("metadata.environment", "PRODUCTION")
    );

    @ClassRule
    public static final MatchingServiceAdapterAppRule integrationMsaWithDefaultTruststoresApplicationRule = new MatchingServiceAdapterAppRule(
            true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            ConfigOverride.config("metadata.environment", "INTEGRATION")
    );

    @Test
    public void prodTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = prodMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow();
        ArrayList<String> hubAliases = Collections.list(metadataConfiguration.getHubTrustStore().orElseThrow().aliases());
        ArrayList<String> idpAliases = Collections.list(metadataConfiguration.getIdpTrustStore().orElseThrow().aliases());
        ArrayList<String> metadataAliases = Collections.list(metadataConfiguration.getTrustStore().aliases());

        assertThat(hubAliases).contains("root-ca", "hub-ca", "root-ca-g3", "core-ca-g3");
        assertThat(idpAliases).contains("root-ca", "idp-ca", "root-ca-g3", "idp-ca-g3");
        assertThat(metadataAliases).contains("root-ca", "metadata-ca", "core-ca-g2", "root-ca-g3", "metadata-ca-g3", "core-ca-g3");
    }

    @Test
    public void integrationTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = integrationMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow();
        ArrayList<String> hubAliases = Collections.list(metadataConfiguration.getHubTrustStore().orElseThrow().aliases());
        ArrayList<String> idpAliases = Collections.list(metadataConfiguration.getIdpTrustStore().orElseThrow().aliases());
        ArrayList<String> metadataAliases = Collections.list(metadataConfiguration.getTrustStore().aliases());

        assertThat(hubAliases).contains(
                "test-root-ca",
                "test-hub-old-ca",
                "test-hub-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-dev-pki-core-ca",
                "test-dev-pki-root-ca"
        );
        assertThat(idpAliases).contains(
                "test-root-ca",
                "test-idp-ca",
                "test-root-ca-g3",
                "test-idp-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-intermediary-ca"
        );
        assertThat(metadataAliases).contains(
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
