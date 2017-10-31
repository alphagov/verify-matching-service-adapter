package uk.gov.ida.matchingserviceadapter.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UnknownUserCreationRequestDto {
    private String hashedPid;
    private LevelOfAssuranceDto levelOfAssurance;

    @SuppressWarnings("unused") //Required by JAXB
    private UnknownUserCreationRequestDto() {}

    public UnknownUserCreationRequestDto(String hashedPid, LevelOfAssuranceDto levelOfAssurance) {
        this.hashedPid = hashedPid;
        this.levelOfAssurance = levelOfAssurance;
    }

    public String getHashedPid() {
        return hashedPid;
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
