package uk.gov.ida.matchingserviceadapter.mappers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.GenderDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.SimpleMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.TransliterableMdsValueDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.UniversalMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyAddressDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.VerifyMatchingDatasetDto;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameToComparator;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameVerifiedComparator;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToMatchingDatasetDtoMapper {

    public VerifyMatchingDatasetDto mapToVerifyMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<TransliterableMdsValue> firstNameValue = matchingDataset.getFirstNames().stream()
                .sorted(new FirstNameToComparator()).min(new FirstNameVerifiedComparator());
        Optional<SimpleMdsValue<String>> middleNameValue = matchingDataset.getMiddleNames().stream().findFirst();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = matchingDataset.getDateOfBirths().stream().findFirst();

        return new VerifyMatchingDatasetDto(
                firstNameValue.map(this::mapToMatchingDatasetTransliterableDto),
                middleNameValue.map(this::mapToMatchingDatasetDto),
                matchingDataset.getSurnames().stream().map(this::mapToMatchingDatasetTransliterableDto).collect(Collectors.toList()),
                matchingDataset.getGender().map(this::mapGender),
                birthDateValue.map(this::mapToMatchingDatasetDto),
                mapVerifyAddresses(matchingDataset.getAddresses()));
    }

    public UniversalMatchingDatasetDto mapToUniversalMatchingDatasetDto(MatchingDataset matchingDataset) {
        Optional<TransliterableMdsValue> firstNameValue = matchingDataset.getFirstNames().stream().findFirst();
        Optional<SimpleMdsValue<String>> middleNameValue = matchingDataset.getMiddleNames().stream().findFirst();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = matchingDataset.getDateOfBirths().stream().findFirst();

        return new UniversalMatchingDatasetDto(
                firstNameValue.map(this::mapToMatchingDatasetTransliterableDto),
                middleNameValue.map(this::mapToMatchingDatasetDto),
                matchingDataset.getSurnames().stream().map(this::mapToMatchingDatasetTransliterableDto).collect(Collectors.toList()),
                matchingDataset.getGender().map(this::mapGender),
                birthDateValue.map(this::mapToMatchingDatasetDto),
                Optional.ofNullable(mapToUniversalAddressDto(matchingDataset.getAddresses())));
    }

    private SimpleMdsValueDto<GenderDto> mapGender(SimpleMdsValue<Gender> simpleMdsValue) {
        return new SimpleMdsValueDto<>(convertToGenderDto(simpleMdsValue.getValue()),
                simpleMdsValue.getFrom(),
                simpleMdsValue.getTo(),
                simpleMdsValue.isVerified());
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

    private <T> SimpleMdsValueDto<T> mapToMatchingDatasetDto(SimpleMdsValue<T> simpleMdsValueOptional) {
        return new SimpleMdsValueDto<>(simpleMdsValueOptional.getValue(),
                simpleMdsValueOptional.getFrom(),
                simpleMdsValueOptional.getTo(),
                simpleMdsValueOptional.isVerified());
    }

    private TransliterableMdsValueDto mapToMatchingDatasetTransliterableDto(TransliterableMdsValue transliterableMdsValue) {
        return new TransliterableMdsValueDto(transliterableMdsValue.getValue(),
                transliterableMdsValue.getNonLatinScriptValue(),
                transliterableMdsValue.getFrom(),
                transliterableMdsValue.getTo(),
                transliterableMdsValue.isVerified());
    }

    private List<VerifyAddressDto> mapVerifyAddresses(List<Address> addresses) {
        return Lists.newArrayList(Collections2.transform(addresses, new Function<Address, VerifyAddressDto>() {
            @Nullable
            @Override
            public VerifyAddressDto apply(Address input) {
                return new VerifyAddressDto(input.getLines(), input.getPostCode(), input.getInternationalPostCode(), input.getUPRN(), input.getFrom(), input.getTo(), input.isVerified());
            }
        }));
    }

    private List<UniversalAddressDto> mapToUniversalAddressDto(List<Address> addresses) {
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
