package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToMatchingDatasetDtoMapper {

    public VerifyMatchingDatasetDto mapToVerifyMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<SimpleMdsValue<String>> firstNameValue = !matchingDataset.getFirstNames().isEmpty() ? Optional.of(matchingDataset.getFirstNames().get(0)) : Optional.empty();
        Optional<SimpleMdsValue<String>> middleNameValue = !matchingDataset.getMiddleNames().isEmpty() ? Optional.of(matchingDataset.getMiddleNames().get(0)) : Optional.empty();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = !matchingDataset.getDateOfBirths().isEmpty() ? Optional.of(matchingDataset.getDateOfBirths().get(0)) : Optional.empty();
        return new VerifyMatchingDatasetDto(
                mapToMatchingDatasetDto(firstNameValue),
                mapToMatchingDatasetDto(middleNameValue),
                mapToMatchingDatasetDto(matchingDataset.getSurnames()),
                mapGender(matchingDataset.getGender()),
                mapToMatchingDatasetDto(birthDateValue),
                mapVerifyAddresses(matchingDataset.getAddresses()));
    }

    public UniversalMatchingDatasetDto mapToUniversalMatchingDatasetDto(EidasMatchingDataset matchingDataset) {
        Optional<SimpleMdsValueDto<String>> firstName = Optional.of(mapEidasValue(matchingDataset.getFirstName()));
        List<SimpleMdsValueDto<String>> surnames = Collections.singletonList(mapEidasValue(matchingDataset.getSurname()));
        Optional<SimpleMdsValueDto<GenderDto>> gender = mapEidasGender(matchingDataset.getGender());
        Optional<SimpleMdsValueDto<LocalDate>> dateOfBirth = Optional.of(mapEidasValue(matchingDataset.getDateOfBirth()));
        Optional<List<UniversalAddressDto>> addresses = matchingDataset.getAddress().isPresent() ?
                Optional.of(mapEidasAddresses(Collections.singletonList(matchingDataset.getAddress().get()))) : Optional.empty();

        return new UniversalMatchingDatasetDto(
                firstName,
                Optional.empty(),
                surnames,
                gender,
                dateOfBirth,
                addresses);
    }

    private <T> SimpleMdsValueDto<T> mapEidasValue(T value) {
        return new SimpleMdsValueDto<>(value, null, null, true);
    }

    private Optional<SimpleMdsValueDto<GenderDto>> mapEidasGender(Optional<Gender> gender) {
        return gender.map(g -> new SimpleMdsValueDto<>(convertToGenderDto(g), null, null, true));
    }

    protected Optional<SimpleMdsValueDto<GenderDto>> mapGender(Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional) {
        if (!simpleMdsValueOptional.isPresent()) {
            return Optional.empty();
        }

        SimpleMdsValue<Gender> simpleMdsValue = simpleMdsValueOptional.get();
        GenderDto genderDto = convertToGenderDto(simpleMdsValue.getValue());
        return Optional.of(new SimpleMdsValueDto<>(genderDto, simpleMdsValue.getFrom(), simpleMdsValue.getTo(), simpleMdsValue.isVerified()));
    }

    private GenderDto convertToGenderDto(Gender gender) {
        GenderDto genderDto;
        switch (gender) {
            case MALE:
                genderDto = GenderDto.MALE;
                break;
            case FEMALE:
                genderDto = GenderDto.FEMALE;
                break;
            case NOT_SPECIFIED:
                genderDto = GenderDto.NOT_SPECIFIED;
                break;
            default:
                throw new IllegalArgumentException("Illegal gender value: '" + gender + "'");
        }
        return genderDto;
    }

    protected <T> Optional<SimpleMdsValueDto<T>> mapToMatchingDatasetDto(Optional<SimpleMdsValue<T>> simpleMdsValueOptional) {
        if (!simpleMdsValueOptional.isPresent()) {
            return Optional.empty();
        }

        SimpleMdsValue<T> simpleMdsValue = simpleMdsValueOptional.get();
        return Optional.of(new SimpleMdsValueDto<>(simpleMdsValue.getValue(), simpleMdsValue.getFrom(), simpleMdsValue.getTo(), simpleMdsValue.isVerified()));
    }

    protected <T> List<SimpleMdsValueDto<T>> mapToMatchingDatasetDto(List<SimpleMdsValue<T>> simpleMdsValues) {
        return Lists.newArrayList(Collections2.transform(simpleMdsValues, new Function<SimpleMdsValue<T>, SimpleMdsValueDto<T>>() {
            @Nullable
            @Override
            public SimpleMdsValueDto<T> apply(SimpleMdsValue<T> input) {
                return new SimpleMdsValueDto<>(input.getValue(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }

    protected List<VerifyAddressDto> mapVerifyAddresses(List<Address> addresses) {
        return Lists.newArrayList(Collections2.transform(addresses, new Function<Address, VerifyAddressDto>() {
            @Nullable
            @Override
            public VerifyAddressDto apply(Address input) {
                return new VerifyAddressDto(input.getLines(), input.getPostCode(), input.getInternationalPostCode(), input.getUPRN(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }

    private List<UniversalAddressDto> mapEidasAddresses(List<Address> addresses) {
        return addresses
                .stream()
                .map(input -> new UniversalAddressDto(input.getLines(),
                        input.getPostCode(),
                        input.getInternationalPostCode(),
                        input.getUPRN(),
                        input.getFrom(),
                        input.getTo(),
                        input.isVerified()))
                .collect(Collectors.toList());
    }
}
