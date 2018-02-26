package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

public class MatchingDatasetToMatchingDatasetDtoMapper {

    public VerifyMatchingDatasetDto mapToVerifyMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<SimpleMdsValue<String>> firstNameValue = !matchingDataset.getFirstNames().isEmpty() ? fromNullable(matchingDataset.getFirstNames().get(0)) : Optional.absent();
        Optional<SimpleMdsValue<String>> middleNameValue = !matchingDataset.getMiddleNames().isEmpty() ? fromNullable(matchingDataset.getMiddleNames().get(0)) : Optional.absent();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = !matchingDataset.getDateOfBirths().isEmpty() ? fromNullable(matchingDataset.getDateOfBirths().get(0)) : Optional.absent();
        return new VerifyMatchingDatasetDto(
                mapToVerifyMatchingDatasetDto(firstNameValue),
                mapToVerifyMatchingDatasetDto(middleNameValue),
                mapToVerifyMatchingDatasetDto(matchingDataset.getSurnames()),
                mapGender(matchingDataset.getGender()),
                mapToVerifyMatchingDatasetDto(birthDateValue),
                mapVerifyAddresses(matchingDataset.getAddresses()));
    }

    protected Optional<SimpleMdsValueDto<GenderDto>> mapGender(Optional<SimpleMdsValue<Gender>> simpleMdsValueOptional) {
        if (!simpleMdsValueOptional.isPresent()) {
            return absent();
        }

        SimpleMdsValue<Gender> simpleMdsValue = simpleMdsValueOptional.get();
        GenderDto genderDto;
        switch (simpleMdsValue.getValue()) {
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
                throw new IllegalArgumentException("Illegal gender value: '" + simpleMdsValue.getValue() + "'");
        }
        return Optional.fromNullable(new SimpleMdsValueDto<>(genderDto, simpleMdsValue.getFrom(), simpleMdsValue.getTo(), simpleMdsValue.isVerified()));
    }

    protected <T> Optional<SimpleMdsValueDto<T>> mapToVerifyMatchingDatasetDto(Optional<SimpleMdsValue<T>> simpleMdsValueOptional) {
        if (!simpleMdsValueOptional.isPresent()) {
            return absent();
        }

        SimpleMdsValue<T> simpleMdsValue = simpleMdsValueOptional.get();
        return Optional.fromNullable(new SimpleMdsValueDto<>(simpleMdsValue.getValue(), simpleMdsValue.getFrom(), simpleMdsValue.getTo(), simpleMdsValue.isVerified()));
    }

    protected <T> List<SimpleMdsValueDto<T>> mapToVerifyMatchingDatasetDto(List<SimpleMdsValue<T>> simpleMdsValues) {
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

    protected List<EidasAddressDto> mapEidasAddresses(List<Address> addresses) {
        return Lists.newArrayList(Collections2.transform(addresses, new Function<Address, EidasAddressDto>() {
            @Nullable
            @Override
            public EidasAddressDto apply(Address input) {
                return new EidasAddressDto(input.getLines(), input.getPostCode(), input.getInternationalPostCode(), input.getUPRN(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }
}
