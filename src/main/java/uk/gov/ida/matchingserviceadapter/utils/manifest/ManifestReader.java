package uk.gov.ida.matchingserviceadapter.utils.manifest;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Singleton
public class ManifestReader {

    private final String manifestLocation;
    private Attributes manifest;

    public ManifestReader(String manifestLocation) {
        this.manifestLocation = manifestLocation;
        this.manifest = initializeManifest();
    }

    public ManifestReader() {
        this("META-INF/MANIFEST.MF");
    }

    private Attributes initializeManifest() {
        Manifest manifest;
        try {
            manifest = new Manifest(getClass().getClassLoader().getResourceAsStream(manifestLocation));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return manifest.getMainAttributes();
    }

    public Attributes getManifest() {
        return manifest;
    }

    public String getValue(String name) {
        return manifest.getValue(name);
    }
}
