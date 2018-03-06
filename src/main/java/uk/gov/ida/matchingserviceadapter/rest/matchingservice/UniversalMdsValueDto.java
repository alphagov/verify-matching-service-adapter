package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class UniversalMdsValueDto<T> extends SimpleMdsValueDto {

    @SuppressWarnings("unused") // needed for JAXB
    public UniversalMdsValueDto() {

    }

    public UniversalMdsValueDto(T value) {
        super(value, null, null, true);
    }
}
