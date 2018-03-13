package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Optional;

// CAUTION!!! CHANGES TO THIS CLASS WILL IMPACT MSA USERS
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UniversalMatchingDatasetDto extends MatchingDatasetDto {

    private Optional<List<UniversalAddressDto>> addresses = Optional.empty();

    @SuppressWarnings("unused") // needed for JAXB
    private UniversalMatchingDatasetDto() {
        super();
    }

    public UniversalMatchingDatasetDto(
            Optional<SimpleMdsValueDto<String>> firstName,
            Optional<SimpleMdsValueDto<String>> middleNames,
            List<SimpleMdsValueDto<String>> surnames,
            Optional<SimpleMdsValueDto<GenderDto>> gender,
            Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth,
            Optional<List<UniversalAddressDto>> addresses) {
        super(firstName, middleNames, surnames, gender, dateOfBirth);

        this.addresses = addresses;
    }

    public Optional<List<UniversalAddressDto>> getAddresses() {
        return addresses;
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