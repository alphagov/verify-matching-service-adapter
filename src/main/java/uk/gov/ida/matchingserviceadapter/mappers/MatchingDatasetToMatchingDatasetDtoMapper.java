package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.AddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.MatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

public class MatchingDatasetToMatchingDatasetDtoMapper {

    public MatchingDatasetDto map(MatchingDataset matchingDataset) {
        Optional<SimpleMdsValue<String>> firstNameValue = !matchingDataset.getFirstNames().isEmpty() ? fromNullable(matchingDataset.getFirstNames().get(0)) : Optional.absent();
        Optional<SimpleMdsValue<String>> middleNameValue = !matchingDataset.getMiddleNames().isEmpty() ? fromNullable(matchingDataset.getMiddleNames().get(0)) : Optional.absent();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = !matchingDataset.getDateOfBirths().isEmpty() ? fromNullable(matchingDataset.getDateOfBirths().get(0)) : Optional.absent();
        return new MatchingDatasetDto(
                map(firstNameValue),
                map(middleNameValue),
                map(matchingDataset.getSurnames()),
                mapGender(matchingDataset.getGender()),
                map(birthDateValue),
                mapAddresses(matchingDataset.getAddresses()));
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

    protected <T> Optional<SimpleMdsValueDto<T>> map(Optional<SimpleMdsValue<T>> simpleMdsValueOptional) {
        if (!simpleMdsValueOptional.isPresent()) {
            return absent();
        }

        SimpleMdsValue<T> simpleMdsValue = simpleMdsValueOptional.get();
        return Optional.fromNullable(new SimpleMdsValueDto<>(simpleMdsValue.getValue(), simpleMdsValue.getFrom(), simpleMdsValue.getTo(), simpleMdsValue.isVerified()));
    }

    protected <T> List<SimpleMdsValueDto<T>> map(List<SimpleMdsValue<T>> simpleMdsValues) {
        return Lists.newArrayList(Collections2.transform(simpleMdsValues, new Function<SimpleMdsValue<T>, SimpleMdsValueDto<T>>() {
            @Nullable
            @Override
            public SimpleMdsValueDto<T> apply(SimpleMdsValue<T> input) {
                return new SimpleMdsValueDto<>(input.getValue(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }

    protected List<AddressDto> mapAddresses(List<Address> addresses) {
        return Lists.newArrayList(Collections2.transform(addresses, new Function<Address, AddressDto>() {
            @Nullable
            @Override
            public AddressDto apply(Address input) {
                return new AddressDto(input.getLines(), input.getPostCode(), input.getInternationalPostCode(), input.getUPRN(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }
}
