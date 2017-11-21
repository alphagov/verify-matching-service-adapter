package uk.gov.ida.matchingserviceadapter.utils.manifest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ManifestReaderTest {

    private ManifestReader manifestReader = new ManifestReader("SAMPLE_MANIFEST.MF");

    @Test
    public void shouldParseManifest() {
        assertThat(manifestReader.getValue("Version-Number")).isEqualTo("493");
    }

}