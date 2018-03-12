package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.builders.EidasMatchingDatasetBuilder;
import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.AddressFactory;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.test.builders.SimpleMdsValueBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.AddressBuilder.anAddress;
import static uk.gov.ida.saml.core.test.builders.MatchingDatasetBuilder.aMatchingDataset;

public class MatchingDatasetToMatchingDatasetDtoMapperTest {

    private MatchingDatasetToMatchingDatasetDtoMapper matchingDatasetToMatchingDatasetDtoMapper;

    @Before
    public void setup() {
        matchingDatasetToMatchingDatasetDtoMapper = new MatchingDatasetToMatchingDatasetDtoMapper();
    }

    @Test
    public void mapGender_shouldMapAbsent() {
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.absent();
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(false);
    }

    @Test
    public void mapGender_shouldMapMale() {
        SimpleMdsValue<Gender> genderSimpleMdsValue = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.MALE).build();
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.fromNullable(genderSimpleMdsValue);
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(true);
        assertThat(simpleMdsValueDtoOptional.get().getValue()).isEqualTo(GenderDto.MALE);
        assertThat(simpleMdsValueDtoOptional.get().getFrom()).isEqualTo(genderSimpleMdsValue.getFrom());
        assertThat(simpleMdsValueDtoOptional.get().getTo()).isEqualTo(genderSimpleMdsValue.getTo());
        assertThat(simpleMdsValueDtoOptional.get().isVerified()).isEqualTo(genderSimpleMdsValue.isVerified());
    }

    @Test
    public void mapGender_shouldMapFemale() {
        SimpleMdsValue<Gender> genderSimpleMdsValue = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).build();
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.fromNullable(genderSimpleMdsValue);
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(true);
        assertThat(simpleMdsValueDtoOptional.get().getValue()).isEqualTo(GenderDto.FEMALE);
    }

    @Test
    public void mapGender_shouldMapNotSpecified() {
        SimpleMdsValue<Gender> genderSimpleMdsValue = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build();
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.fromNullable(genderSimpleMdsValue);
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(true);
        assertThat(simpleMdsValueDtoOptional.get().getValue()).isEqualTo(GenderDto.NOT_SPECIFIED);
    }

    @Test
    public void map_shouldMapGenericSimpleMdsValue() {
        SimpleMdsValue<String> simpleMdsValue = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("hello").build();
        Optional<SimpleMdsValue<String>> simpleMdsValueOptional = Optional.fromNullable(simpleMdsValue);
        Optional<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.mapToMatchingDatasetDto(simpleMdsValueOptional);

        assertThat(result.isPresent()).isEqualTo(true);
        assertThat(result.get().getValue()).isEqualTo("hello");
        assertThat(result.get().getFrom()).isEqualTo(simpleMdsValue.getFrom());
        assertThat(result.get().getTo()).isEqualTo(simpleMdsValue.getTo());
        assertThat(result.get().isVerified()).isEqualTo(simpleMdsValue.isVerified());
    }

    @Test
    public void map_shouldMapListOfGenericSimpleMdsValues() {
        SimpleMdsValue<String> simpleMdsValue = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("hello").build();
        List<SimpleMdsValue<String>> simpleMdsValueOptional = singletonList(simpleMdsValue);
        List<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.mapToMatchingDatasetDto(simpleMdsValueOptional);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getValue()).isEqualTo("hello");
        assertThat(result.get(0).getFrom()).isEqualTo(simpleMdsValue.getFrom());
        assertThat(result.get(0).getTo()).isEqualTo(simpleMdsValue.getTo());
        assertThat(result.get(0).isVerified()).isEqualTo(simpleMdsValue.isVerified());
    }

    @Test
    public void map_shouldMapGenericSimpleMdsValueAbsent() {
        Optional<SimpleMdsValue<String>> simpleMdsValueOptional = Optional.absent();
        Optional<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.mapToMatchingDatasetDto(simpleMdsValueOptional);

        assertThat(result.isPresent()).isEqualTo(false);
    }

    @Test
    public void map_shouldCombineCurrentAndPreviousAddressesFromAMatchingDatasetToSingleAddressField() {
        String currentAddressLine = "line-1";
        String previousAddressLine = "previousAddressLine";
        List<Address> currentAddress = singletonList(new AddressFactory().create(singletonList(currentAddressLine), "post-code", "international-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        List<Address> previousAddress = singletonList(new AddressFactory().create(singletonList(previousAddressLine), "post-code", "international-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        MatchingDataset matchingDataset = aMatchingDataset()
                .withCurrentAddresses(currentAddress)
                .withPreviousAddresses(previousAddress)
                .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        List<VerifyAddressDto> addresses = matchingDatasetDto.getAddresses();
        assertThat(addresses).hasSize(2);

        List<String> addressLines = new ArrayList<>();
        for(AddressDto addressDto : addresses){
            addressLines.add(addressDto.getLines().get(0));
        }
        assertThat(addressLines).contains(currentAddressLine);
        assertThat(addressLines).contains(previousAddressLine);
    }

    @Test
    public void map_shouldPutCurrentAddressBeforePreviousAddressesWhenMappingToASingleAddressField() {
        String currentAddressLine = "line-1";
        String previousAddressLine = "previousAddressLine";
        List<Address> currentAddress = singletonList(new AddressFactory().create(singletonList(currentAddressLine), "post-code", "international-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        List<Address> previousAddress = singletonList(new AddressFactory().create(singletonList(previousAddressLine), "post-code", "international-postcode", "uprn", DateTime.parse("1999-03-15"), DateTime.parse("2000-02-09"), true));
        MatchingDataset matchingDataset = aMatchingDataset()
            .withPreviousAddresses(previousAddress)
            .withCurrentAddresses(currentAddress)
            .build();

        VerifyMatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.mapToVerifyMatchingDatasetDto(matchingDataset);

        List<VerifyAddressDto> addresses = matchingDatasetDto.getAddresses();
        assertThat(addresses).hasSize(2);

        List<String> addressLines = new ArrayList<>();
        for(AddressDto addressDto : addresses){
            addressLines.add(addressDto.getLines().get(0));
        }
        assertThat(addressLines.get(0)).isEqualTo(currentAddressLine);
        assertThat(addressLines.get(1)).isEqualTo(previousAddressLine);
    }

    @Test
    public void mapVerifyAddress_shouldMapAddress() {
        Address address = anAddress()
                .withLines(singletonList("line1"))
                .withVerified(false)
                .withUPRN("uprn")
                .withFromDate(DateTime.now())
                .withToDate(DateTime.now().plusDays(1))
                .withInternationalPostCode("int-post-code")
                .withPostCode("postcode")
                .build();
        List<VerifyAddressDto> result = matchingDatasetToMatchingDatasetDtoMapper.mapVerifyAddresses(singletonList(address));

        assertThat(result.size()).isEqualTo(1);
        VerifyAddressDto addressDto = result.get(0);
        assertThat(addressDto.getLines().size()).isEqualTo(1);
        assertThat(addressDto.getLines().get(0)).isEqualTo(address.getLines().get(0));
        assertThat(addressDto.isVerified()).isEqualTo(address.isVerified());
        assertThat(addressDto.getUPRN()).isEqualTo(address.getUPRN());
        assertThat(addressDto.getFromDate()).isEqualTo(address.getFrom());
        assertThat(addressDto.getToDate()).isEqualTo(address.getTo());
        assertThat(addressDto.getInternationalPostCode()).isEqualTo(addressDto.getInternationalPostCode());
        assertThat(addressDto.getPostCode()).isEqualTo(addressDto.getPostCode());
    }

    @Test
    public void mapEidasDatasetToUniversalDatasetDto_shouldMapNames() {
        EidasMatchingDataset eidasMatchingDataset = EidasMatchingDatasetBuilder.anEidasMatchingDataset()
                .withFirstName("input-first-name")
                .withSurname("input-surname")
                .build();

        UniversalMatchingDatasetDto result =
                matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(eidasMatchingDataset);

        assertThat(result.getFirstName().get().getValue()).isEqualTo(eidasMatchingDataset.getFirstName());
        assertHasEidasDefaultValues(result.getFirstName().get());
        assertThat(result.getMiddleNames().isPresent()).isFalse();
        assertThat(result.getSurnames().get(0).getValue()).isEqualTo(eidasMatchingDataset.getSurname());
        assertHasEidasDefaultValues(result.getSurnames().get(0));
    }

    @Test
    public void mapEidasDatasetToUniversalDatasetDto_shouldMapGender() {
        EidasMatchingDataset eidasMatchingDataset = EidasMatchingDatasetBuilder.anEidasMatchingDataset()
                .withGender(Gender.MALE)
                .build();

        UniversalMatchingDatasetDto result =
                matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(eidasMatchingDataset);

        assertThat(result.getGender().get().getValue()).isEqualTo(GenderDto.MALE);
        assertHasEidasDefaultValues(result.getGender().get());
    }

    @Test
    public void mapEidasDatasetToUniversalDatasetDto_shouldMapDateOfBirth() {
        EidasMatchingDataset eidasMatchingDataset = EidasMatchingDatasetBuilder.anEidasMatchingDataset()
                .withDateOfBirth(LocalDate.now())
                .build();

        UniversalMatchingDatasetDto result =
                matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(eidasMatchingDataset);

        assertThat(result.getDateOfBirth().get().getValue()).isEqualTo(eidasMatchingDataset.getDateOfBirth());
        assertHasEidasDefaultValues(result.getDateOfBirth().get());
    }

    @Test
    public void mapEidasDatasetToUniversalDatasetDto_shouldMapAddresses() {
        Address address = anAddress()
                .withLines(singletonList("line1"))
                .withVerified(false)
                .withUPRN("uprn")
                .withFromDate(DateTime.now())
                .withToDate(DateTime.now().plusDays(1))
                .withInternationalPostCode("int-post-code")
                .withPostCode("postcode")
                .build();

        EidasMatchingDataset eidasMatchingDataset = EidasMatchingDatasetBuilder.anEidasMatchingDataset()
                .withAddress(address)
                .build();

        UniversalMatchingDatasetDto universalMatchingDatasetDto =
                matchingDatasetToMatchingDatasetDtoMapper.mapToUniversalMatchingDatasetDto(eidasMatchingDataset);

        List<UniversalAddressDto> result = universalMatchingDatasetDto.getAddresses().get();
        assertThat(result.size()).isEqualTo(1);
        UniversalAddressDto addressDto = result.get(0);
        assertThat(addressDto.getLines().size()).isEqualTo(1);
        assertThat(addressDto.getLines().get(0)).isEqualTo(address.getLines().get(0));
        assertThat(addressDto.isVerified()).isEqualTo(address.isVerified());
        assertThat(addressDto.getUPRN()).isEqualTo(address.getUPRN());
        assertThat(addressDto.getFrom()).isEqualTo(address.getFrom());
        assertThat(addressDto.getTo()).isEqualTo(address.getTo());
        assertThat(addressDto.getInternationalPostCode()).isEqualTo(addressDto.getInternationalPostCode());
        assertThat(addressDto.getPostCode()).isEqualTo(addressDto.getPostCode());
    }

    private void assertHasEidasDefaultValues(SimpleMdsValueDto simpleMdsValueDto) {
        assertThat(simpleMdsValueDto.getFrom()).isNull();
        assertThat(simpleMdsValueDto.getTo()).isNull();
        assertThat(simpleMdsValueDto.isVerified()).isTrue();
    }
}