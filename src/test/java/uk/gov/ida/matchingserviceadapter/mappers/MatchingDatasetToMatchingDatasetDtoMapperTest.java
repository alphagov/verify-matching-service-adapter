package uk.gov.ida.matchingserviceadapter.mappers;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameToComparator;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameVerifiedComparator;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aCurrentAddress;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.aHistoricalAddress;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_FROM_DATE;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_FROM_DATE;
import static uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder.DEFAULT_HISTORICAL_TO_DATE;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

public class MatchingDatasetToMatchingDatasetDtoMapperTest {

    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper = new MatchingDatasetToMatchingDatasetDtoMapper();

    @Test
    public void shouldMapToVerifyMatchingDatasetDto() {
        LocalDate dob = new LocalDate(1970, 1, 2);
        LocalDate oldDob = new LocalDate(1970, 2, 1);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").withVerifiedStatus(true).build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Bob").build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").withVerifiedStatus(true).build())
                .addSurname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Smith").build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aHistoricalSimpleMdsValue().withValue(oldDob).build())
                .withCurrentAddresses(asList(aCurrentAddress().withPostCode("AA12BB").build()))
                .withPreviousAddresses(asList(aHistoricalAddress().withPostCode("CC12DD").build()))
                .withGender(SimpleMdsValueBuilder.<Gender>aCurrentSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build())
                .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, DEFAULT_FROM_DATE, null, true));
        // Check entire surname history is included
        assertThat(matchingDatasetDto.getSurnames().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Bloggs", null, DEFAULT_FROM_DATE, null, true));
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Smith", null, DEFAULT_HISTORICAL_FROM_DATE, DEFAULT_HISTORICAL_TO_DATE, false));
        // Check that only the current date of birth is included
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, DEFAULT_FROM_DATE, null, false));
        // Check entire address history is included
        assertThat(matchingDatasetDto.getAddresses().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getAddresses().stream().filter(a -> a.getPostCode().equals(Optional.of("AA12BB"))).findFirst()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().stream().filter(a -> a.getPostCode().equals(Optional.of("CC12DD"))).findFirst()).isPresent();
        // Check that gender is included
        assertThat(matchingDatasetDto.getGender()).contains(new SimpleMdsValueDto<>(GenderDto.NOT_SPECIFIED, DEFAULT_FROM_DATE, null, false));
    }

    @Test
    public void shouldMapCurrentFirstName() {
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("historical unverified: expected fourth").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("current unverified: expected second").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("historical verified: expected third").withVerifiedStatus(true).build())
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("current verified: expected first").withVerifiedStatus(true).build())
                .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("current verified: expected first", null, DEFAULT_FROM_DATE, null, true));
    }

    @Test
    public void comparatorPrioritisesVerifiedThenCurrent() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("historical unverified: expected seventh", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected fifth", null, false));
        firstNames.add(buildFirstName("historical verified: expected third", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected first", null, true));
        firstNames.add(buildFirstName("historical unverified: expected eighth", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected sixth", null, false));
        firstNames.add(buildFirstName("historical verified: expected fourth", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected second", null, true));
        firstNames.sort(MatchingDatasetToMatchingDatasetDtoMapper.getFirstNameComparator());
        assertEquals("current verified: expected first", firstNames.get(0).getValue());
        assertEquals("current verified: expected second", firstNames.get(1).getValue());
        assertEquals("historical verified: expected third", firstNames.get(2).getValue());
        assertEquals("historical verified: expected fourth", firstNames.get(3).getValue());
        assertEquals("current unverified: expected fifth", firstNames.get(4).getValue());
        assertEquals("current unverified: expected sixth", firstNames.get(5).getValue());
        assertEquals("historical unverified: expected seventh", firstNames.get(6).getValue());
        assertEquals("historical unverified: expected eighth", firstNames.get(7).getValue());
    }

    @Test
    public void comparatorMatchesExistingBehaviour() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("historical unverified: expected seventh", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected fifth", null, false));
        firstNames.add(buildFirstName("historical verified: expected third", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected first", null, true));
        firstNames.add(buildFirstName("historical unverified: expected eighth", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected sixth", null, false));
        firstNames.add(buildFirstName("historical verified: expected fourth", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected second", null, true));
        firstNames.sort(new FirstNameToComparator());
        firstNames.sort(new FirstNameVerifiedComparator());
        assertEquals("current verified: expected first", firstNames.get(0).getValue());
        assertEquals("current verified: expected second", firstNames.get(1).getValue());
        assertEquals("historical verified: expected third", firstNames.get(2).getValue());
        assertEquals("historical verified: expected fourth", firstNames.get(3).getValue());
        assertEquals("current unverified: expected fifth", firstNames.get(4).getValue());
        assertEquals("current unverified: expected sixth", firstNames.get(5).getValue());
        assertEquals("historical unverified: expected seventh", firstNames.get(6).getValue());
        assertEquals("historical unverified: expected eighth", firstNames.get(7).getValue());
    }

    @Test
    public void shouldMapToUniversalMatchingDatasetDto() {
        LocalDate dob = new LocalDate(1970, 1, 2);
        LocalDate oldDob = new LocalDate(1970, 2, 1);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").build())
                .addFirstname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Bob").build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").build())
                .addSurname(SimpleMdsValueBuilder.<String>aHistoricalSimpleMdsValue().withValue("Smith").build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aHistoricalSimpleMdsValue().withValue(oldDob).build())
                .withCurrentAddresses(asList(aCurrentAddress().withPostCode("AA12BB").build()))
                .withPreviousAddresses(asList(aHistoricalAddress().withPostCode("CC12DD").build()))
                .withGender(SimpleMdsValueBuilder.<Gender>aCurrentSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build())
                .build();

        UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset);

        // Check that only the current first name is included
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, DEFAULT_FROM_DATE, null, false));
        // Check entire surname history is included
        assertThat(matchingDatasetDto.getSurnames().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Bloggs", null, DEFAULT_FROM_DATE, null, false));
        assertThat(matchingDatasetDto.getSurnames()).contains(new TransliterableMdsValueDto("Smith", null, DEFAULT_HISTORICAL_FROM_DATE, DEFAULT_HISTORICAL_TO_DATE, false));
        // Check that only the current date of birth is included
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, DEFAULT_FROM_DATE, null, false));
        // Check entire address history is included
        assertThat(matchingDatasetDto.getAddresses()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().get().size()).isEqualTo(2);
        assertThat(matchingDatasetDto.getAddresses().get().stream().filter(a -> a.getPostCode().equals(Optional.of("AA12BB"))).findFirst()).isPresent();
        assertThat(matchingDatasetDto.getAddresses().get().stream().filter(a -> a.getPostCode().equals(Optional.of("CC12DD"))).findFirst()).isPresent();
        // Check that gender is included
        assertThat(matchingDatasetDto.getGender()).contains(new SimpleMdsValueDto<>(GenderDto.NOT_SPECIFIED, DEFAULT_FROM_DATE, null, false));
    }

    @Test
    public void shouldMapToUniversalMatchingDatasetDtoWithTypicalEidasDataset() {
        LocalDate dob = new LocalDate(1970, 1, 2);
        MatchingDataset matchingDataset = aMatchingDataset()
                .addFirstname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Joe").withFrom(null).withVerifiedStatus(true).build())
                .addSurname(SimpleMdsValueBuilder.<String>aCurrentSimpleMdsValue().withValue("Bloggs").withFrom(null).withVerifiedStatus(true).build())
                .addDateOfBirth(SimpleMdsValueBuilder.<LocalDate>aCurrentSimpleMdsValue().withValue(dob).withFrom(null).withVerifiedStatus(true).build())
                .build();

        UniversalMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(matchingDataset);

        // eIDAS matching datasets will never have a from or to date and will always be verified
        assertThat(matchingDatasetDto.getFirstName()).contains(new TransliterableMdsValueDto("Joe", null, null, null, true));
        assertThat(matchingDatasetDto.getSurnames()).containsOnly(new TransliterableMdsValueDto("Bloggs", null, null, null, true));
        assertThat(matchingDatasetDto.getDateOfBirth()).contains(new SimpleMdsValueDto<>(dob, null, null, true));
    }

    private TransliterableMdsValue buildFirstName(String name, DateTime to, boolean verified) {
        SimpleMdsValue<String> simpleValue = new SimpleMdsValue<>(name, null, to, verified);
        return new TransliterableMdsValue(simpleValue);
    }
}