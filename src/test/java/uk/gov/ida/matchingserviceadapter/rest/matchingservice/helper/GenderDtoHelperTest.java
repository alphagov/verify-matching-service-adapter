package uk.gov.ida.matchingserviceadapter.rest.matchingservice.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;

import static org.assertj.core.api.Assertions.assertThat;

public class GenderDtoHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldMapUnspecifiedToNotSpecified() {

        GenderDto verifyGender = GenderDtoHelper.convertToVerifyGenderDto(GenderDtoHelper.UNSPECIFIED_VALUE);

        assertThat(verifyGender).isEqualTo(GenderDto.NOT_SPECIFIED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenValueUnexpected() {

        GenderDtoHelper.convertToVerifyGenderDto("unexpected value");

        expectedException.expectMessage("No enum constant uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto.unexpected value");
    }

}