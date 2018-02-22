package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
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
    public void mapGender_shouldMapAbsent() throws Exception {
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.absent();
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(false);
    }

    @Test
    public void mapGender_shouldMapMale() throws Exception {
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
    public void mapGender_shouldMapFemale() throws Exception {
        SimpleMdsValue<Gender> genderSimpleMdsValue = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.FEMALE).build();
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.fromNullable(genderSimpleMdsValue);
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(true);
        assertThat(simpleMdsValueDtoOptional.get().getValue()).isEqualTo(GenderDto.FEMALE);
    }

    @Test
    public void mapGender_shouldMapNotSpecified() throws Exception {
        SimpleMdsValue<Gender> genderSimpleMdsValue = SimpleMdsValueBuilder.<Gender>aSimpleMdsValue().withValue(Gender.NOT_SPECIFIED).build();
        Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional = Optional.fromNullable(genderSimpleMdsValue);
        Optional<SimpleMdsValueDto<GenderDto>> simpleMdsValueDtoOptional = matchingDatasetToMatchingDatasetDtoMapper.mapGender(simpleMdsValueOptional);

        assertThat(simpleMdsValueDtoOptional.isPresent()).isEqualTo(true);
        assertThat(simpleMdsValueDtoOptional.get().getValue()).isEqualTo(GenderDto.NOT_SPECIFIED);
    }

    @Test
    public void map_shouldMapGenericSimpleMdsValue() throws Exception {
        SimpleMdsValue<String> simpleMdsValue = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("hello").build();
        Optional<SimpleMdsValue<String>> simpleMdsValueOptional = Optional.fromNullable(simpleMdsValue);
        Optional<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.map(simpleMdsValueOptional);

        assertThat(result.isPresent()).isEqualTo(true);
        assertThat(result.get().getValue()).isEqualTo("hello");
        assertThat(result.get().getFrom()).isEqualTo(simpleMdsValue.getFrom());
        assertThat(result.get().getTo()).isEqualTo(simpleMdsValue.getTo());
        assertThat(result.get().isVerified()).isEqualTo(simpleMdsValue.isVerified());
    }

    @Test
    public void map_shouldMapListOfGenericSimpleMdsValues() throws Exception {
        SimpleMdsValue<String> simpleMdsValue = SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("hello").build();
        List<SimpleMdsValue<String>> simpleMdsValueOptional = singletonList(simpleMdsValue);
        List<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.map(simpleMdsValueOptional);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getValue()).isEqualTo("hello");
        assertThat(result.get(0).getFrom()).isEqualTo(simpleMdsValue.getFrom());
        assertThat(result.get(0).getTo()).isEqualTo(simpleMdsValue.getTo());
        assertThat(result.get(0).isVerified()).isEqualTo(simpleMdsValue.isVerified());
    }

    @Test
    public void map_shouldMapGenericSimpleMdsValueAbsent() throws Exception {
        Optional<SimpleMdsValue<String>> simpleMdsValueOptional = Optional.absent();
        Optional<SimpleMdsValueDto<String>> result = matchingDatasetToMatchingDatasetDtoMapper.map(simpleMdsValueOptional);

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

        MatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.map(matchingDataset);

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

        MatchingDatasetDto matchingDatasetDto = matchingDatasetToMatchingDatasetDtoMapper.map(matchingDataset);

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
    public void mapVerifyAddress_shouldMapAddress() throws Exception {
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
    public void mapEidasAddress_shouldMapAddress() throws Exception {
        Address address = anAddress()
                .withLines(singletonList("line1"))
                .withVerified(false)
                .withUPRN("uprn")
                .withFromDate(DateTime.now())
                .withToDate(DateTime.now().plusDays(1))
                .withInternationalPostCode("int-post-code")
                .withPostCode("postcode")
                .build();
        List<EidasAddressDto> result = matchingDatasetToMatchingDatasetDtoMapper.mapEidasAddresses(singletonList(address));

        assertThat(result.size()).isEqualTo(1);
        EidasAddressDto addressDto = result.get(0);
        assertThat(addressDto.getLines().size()).isEqualTo(1);
        assertThat(addressDto.getLines().get(0)).isEqualTo(address.getLines().get(0));
        assertThat(addressDto.isVerified()).isEqualTo(address.isVerified());
        assertThat(addressDto.getUPRN()).isEqualTo(address.getUPRN());
        assertThat(addressDto.getFrom()).isEqualTo(address.getFrom());
        assertThat(addressDto.getTo()).isEqualTo(address.getTo());
        assertThat(addressDto.getInternationalPostCode()).isEqualTo(addressDto.getInternationalPostCode());
        assertThat(addressDto.getPostCode()).isEqualTo(addressDto.getPostCode());
    }
}
