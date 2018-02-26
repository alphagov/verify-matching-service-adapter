package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;


// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class VerifyMatchingServiceRequestDto {

    private VerifyMatchingDatasetDto matchingDataset;
    private Optional<Cycle3DatasetDto> cycle3Dataset = Optional.absent();
    private EidasMatchingDatasetDto eidasDataset;
    private String hashedPid;
    private String matchId;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused")//Needed by JAXB
    private VerifyMatchingServiceRequestDto() {}

    public VerifyMatchingServiceRequestDto(
            VerifyMatchingDatasetDto matchingDataset,
            Optional<Cycle3DatasetDto> cycle3Dataset,
            String hashedPid,
            String matchId,
            LevelOfAssuranceDto levelOfAssurance) {

        this(cycle3Dataset, hashedPid, matchId, levelOfAssurance);
        this.matchingDataset = matchingDataset;
    }

    public VerifyMatchingServiceRequestDto(
        EidasMatchingDatasetDto eidasDataset,
        Optional<Cycle3DatasetDto> cycle3Dataset,
        String hashedPid,
        String matchId,
        LevelOfAssuranceDto levelOfAssurance) {

        this(cycle3Dataset, hashedPid, matchId, levelOfAssurance);
        this.eidasDataset = eidasDataset;
    }

    private VerifyMatchingServiceRequestDto(Optional<Cycle3DatasetDto> cycle3Dataset,
                                            String hashedPid,
                                            String matchId,
                                            LevelOfAssuranceDto levelOfAssurance) {
        this.cycle3Dataset = cycle3Dataset;
        this.hashedPid = hashedPid;
        this.matchId = matchId;
        this.levelOfAssurance = levelOfAssurance;
    }

    public VerifyMatchingDatasetDto getMatchingDataset() {
        return matchingDataset;
    }

    public String getHashedPid() {
        return hashedPid;
    }

    @SuppressWarnings("unused") // this is for an interface
    public String getMatchId() {
        return matchId;
    }

    public Optional<Cycle3DatasetDto> getCycle3Dataset() {
        return cycle3Dataset;
    }

    public LevelOfAssuranceDto getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public EidasMatchingDatasetDto getEidasDataset() {
        return eidasDataset;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
