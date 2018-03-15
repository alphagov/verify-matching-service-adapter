package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

import java.util.Optional;


// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public abstract class MatchingServiceRequestDto {

    private Optional<Cycle3DatasetDto> cycle3Dataset = Optional.empty();
    private String hashedPid;
    private String matchId;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused")//Needed by JAXB
    protected MatchingServiceRequestDto() {}

    protected MatchingServiceRequestDto(Optional<Cycle3DatasetDto> cycle3Dataset,
                                      String hashedPid,
                                      String matchId,
                                      LevelOfAssuranceDto levelOfAssurance) {
        this.cycle3Dataset = cycle3Dataset;
        this.hashedPid = hashedPid;
        this.matchId = matchId;
        this.levelOfAssurance = levelOfAssurance;
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

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
