package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.DateTime;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class TransliterableMdsValueDto extends SimpleMdsValueDto<String> {

    private String nonLatinScriptValue;

    @SuppressWarnings("unused") // needed for JAXB
    public TransliterableMdsValueDto() {

    }

    public TransliterableMdsValueDto(String value, String nonLatinScriptValue) {
        super(value, null, null, true);
        this.nonLatinScriptValue = nonLatinScriptValue;
    }

    public TransliterableMdsValueDto(String value, String nonLatinScriptValue, DateTime from, DateTime to, boolean verified) {
        super(value, from, to, verified);
        this.nonLatinScriptValue = nonLatinScriptValue;
    }

    public String getNonLatinScriptValue() {
        return nonLatinScriptValue;
    }
}
