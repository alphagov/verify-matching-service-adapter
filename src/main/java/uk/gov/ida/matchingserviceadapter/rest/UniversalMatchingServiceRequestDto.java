package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.Cycle3DatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;


// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UniversalMatchingServiceRequestDto extends MatchingServiceRequestDto {

    private UniversalMatchingDatasetDto matchingDataset;

    @SuppressWarnings("unused")//Needed by JAXB
    private UniversalMatchingServiceRequestDto() { super(); }

    public UniversalMatchingServiceRequestDto(
            UniversalMatchingDatasetDto matchingDataset,
            Optional<Cycle3DatasetDto> cycle3Dataset,
            String hashedPid,
            String matchId,
            LevelOfAssuranceDto levelOfAssurance) {

        super(cycle3Dataset, hashedPid, matchId, levelOfAssurance);
        this.matchingDataset = matchingDataset;
    }

    public UniversalMatchingDatasetDto getMatchingDataset() {
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
