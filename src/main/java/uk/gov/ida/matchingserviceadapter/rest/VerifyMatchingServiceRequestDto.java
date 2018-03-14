package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;

import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VerifyMatchingServiceRequestDto extends MatchingServiceRequestDto {

    private VerifyMatchingDatasetDto matchingDataset;

    @SuppressWarnings("unused")//Needed by JAXB
    private VerifyMatchingServiceRequestDto() { super(); }

    public VerifyMatchingServiceRequestDto(
            VerifyMatchingDatasetDto matchingDataset,
            Optional<Cycle3DatasetDto> cycle3Dataset,
            String hashedPid,
            String matchId,
            LevelOfAssuranceDto levelOfAssurance) {

        super(cycle3Dataset, hashedPid, matchId, levelOfAssurance);
        this.matchingDataset = matchingDataset;
    }

    public VerifyMatchingDatasetDto getMatchingDataset() {
        return matchingDataset;
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
