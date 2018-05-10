package uk.gov.ida.matchingserviceadapter.domain;

import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

import static java.util.Arrays.stream;

public enum EidasLoa {
    LOW("http://eidas.europa.eu/LoA/low", LevelOfAssuranceDto.LEVEL_1),

    SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial", LevelOfAssuranceDto.LEVEL_2),

    HIGH("http://eidas.europa.eu/LoA/high", LevelOfAssuranceDto.LEVEL_4);

    private final String valueUri;
    private final LevelOfAssuranceDto verifyLoa;

    EidasLoa(String valueUri, LevelOfAssuranceDto verifyLoa) {
        this.valueUri = valueUri;
        this.verifyLoa = verifyLoa;
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

    public LevelOfAssuranceDto getVerifyLoa() {
        return verifyLoa;
    }
}
