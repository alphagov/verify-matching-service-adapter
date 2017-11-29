package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.LocalDate;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.EidasLoa;
import uk.gov.ida.matchingserviceadapter.domain.EidasToVerifyLoaTransformer;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.IdaConstants.Eidas_Attributes;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.extensions.eidas.BirthName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.Gender;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;
import uk.gov.ida.saml.core.extensions.eidas.PlaceOfBirth;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Optional.of;

public class EidasMatchingRequestToLMSRequestTransform implements Function<MatchingServiceRequestContext, MatchingServiceRequestDto> {

    private EidasToVerifyLoaTransformer eidasToVerifyLoaTransformer = new EidasToVerifyLoaTransformer();
    private UserIdHashFactory userIdHashFactory;

    public EidasMatchingRequestToLMSRequestTransform(UserIdHashFactory userIdHashFactory) {
        this.userIdHashFactory = userIdHashFactory;
    }

    @Override
    public MatchingServiceRequestDto apply(MatchingServiceRequestContext matchingServiceRequestContext) {
        Assertion assertion = matchingServiceRequestContext.getAssertions().get(0);
        List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();

        return new MatchingServiceRequestDto(extractEidasMatchingDataset(attributes),
            com.google.common.base.Optional.absent(),
            extractAndHashPid(assertion),
            matchingServiceRequestContext.getAttributeQuery().getID(),
            extractVerifyLoa(assertion));
    }

    private LevelOfAssuranceDto extractVerifyLoa(Assertion assertion) {
        return eidasToVerifyLoaTransformer.apply(EidasLoa.valueOfUri(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()));
    }

    private String extractAndHashPid(Assertion assertion) {
        String euPid = assertion.getAttributeStatements()
            .get(0)
            .getAttributes()
            .stream()
            .filter(a -> a.getName().equals(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(v -> ((PersonIdentifier) v).getPersonIdentifier())
            .get();
        return userIdHashFactory.hashId(assertion.getIssuer().getValue(),
            euPid,
            of(AuthnContext.valueOf(extractVerifyLoa(assertion).name())));
    }

    private EidasMatchingDataset extractEidasMatchingDataset(List<Attribute> attributes) {
        Optional<LocalDate> dob = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.DateOfBirth.NAME), DateOfBirth.class, DateOfBirth::getDateOfBirth);
        Optional<String> givenName = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.FirstName.NAME), CurrentGivenName.class, CurrentGivenName::getFirstName);
        Optional<String> familyName = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.FamilyName.NAME), CurrentFamilyName.class, CurrentFamilyName::getFamilyName);
        Optional<String> birthName = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.BirthName.NAME), BirthName.class, BirthName::getBirthName);
        Optional<String> placeOfBirth = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.PlaceOfBirth.NAME), PlaceOfBirth.class, PlaceOfBirth::getPlaceOfBirth);
        Optional<String> gender = getAttributeValue(attributes, a -> a.getName().equals(Eidas_Attributes.Gender.NAME), Gender.class, Gender::getValue);

        return new EidasMatchingDataset(null,
            dob.orElse(null),
            givenName.orElse(null),
            familyName.orElse(null),
            birthName.orElse(null),
            gender.orElse(null),
            placeOfBirth.orElse(null)
        );
    }

    private <T, V> Optional<V> getAttributeValue(List<Attribute> attributes, Predicate<Attribute> filter, Class<T> clazz, Function<T, V> transformer) {
        return attributes.stream()
            .filter(filter)
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(clazz::cast)
            .map(transformer);
    }
}
