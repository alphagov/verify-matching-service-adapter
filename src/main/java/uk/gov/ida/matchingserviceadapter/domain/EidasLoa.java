package uk.gov.ida.matchingserviceadapter.domain;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum EidasLoa {
    LOW("http://eidas.europa.eu/LoA/low"),

    SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial"),

    HIGH("http://eidas.europa.eu/LoA/high");

    private final String valueUri;

    EidasLoa(String valueUri) {
        this.valueUri = valueUri;
    }

    public String getValueUri() {
        return this.valueUri;
    }

    public static EidasLoa valueOfUri(String eidasLoaUri) {
        return stream(EidasLoa.values())
            .filter(eidasLoa -> eidasLoa.getValueUri().equals(eidasLoaUri))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Unable to find eIDAS LOA for value URI [%s]", eidasLoaUri)));
    }
}
