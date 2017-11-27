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
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.Gender;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Optional.of;

public class EidasMatchingRequestToLMSRequestTransform implements Function<MatchingServiceRequestContext, MatchingServiceRequestDto> {
    public static final String GIVEN_NAME="http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName";
    public static final String FAMILY_NAME="http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName";
    public static final String BIRTH_NAME="http://eidas.europa.eu/attributes/naturalperson/BirthName";
    public static final String PLACE_OF_BIRTH="http://eidas.europa.eu/attributes/naturalperson/PlaceOfBirth";
    public static final String DOB="http://eidas.europa.eu/attributes/naturalperson/DateOfBirth";
    public static final String PID="http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier";
    public static final String CURRENT_ADDRESS="http://eidas.europa.eu/attributes/naturalperson/CurrentAddress";
    public static final String GENDER="http://eidas.europa.eu/attributes/naturalperson/Gender";

    private EidasToVerifyLoaTransformer eidasToVerifyLoaTransformer = new EidasToVerifyLoaTransformer();
    private UserIdHashFactory userIdHashFactory;

    public EidasMatchingRequestToLMSRequestTransform(UserIdHashFactory userIdHashFactory) {
        this.userIdHashFactory = userIdHashFactory;
    }

    @Override
    public MatchingServiceRequestDto apply(MatchingServiceRequestContext matchingServiceRequestContext) {
        Assertion assertion = matchingServiceRequestContext.getAssertions().get(0);

        return new MatchingServiceRequestDto((EidasMatchingDataset) extractEidasMatchingDataset(assertion),
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
            .map(v -> ((PersonIdentifier)v).getPersonIdentifier())
            .get();
        return userIdHashFactory.hashId(assertion.getIssuer().getValue(),
                                 euPid,
                                 of(AuthnContext.valueOf(extractVerifyLoa(assertion).name())));
    }

    private EidasMatchingDataset extractEidasMatchingDataset(Assertion assertion) {
        List<Attribute> attributes = assertion.getAttributeStatements()
            .get(0)
            .getAttributes();
        Optional<LocalDate> dob = attributes.stream()
            .filter(a -> a.getName().equals(DOB))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(v -> ((DateOfBirth) v))
            .map(DateOfBirth::getDateOfBirth);

        Optional<String> givenName = attributes.stream()
            .filter(a -> a.getName().equals(GIVEN_NAME))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(v -> ((CurrentGivenName) v))
            .map(CurrentGivenName::getFirstName);

        Optional<String> familyName = attributes.stream()
            .filter(a -> a.getName().equals(FAMILY_NAME))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(v -> ((CurrentFamilyName) v))
            .map(CurrentFamilyName::getFamilyName);

        Optional<String> birthName = attributes.stream()
            .filter(a -> a.getName().equals(BIRTH_NAME))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(av -> av.getDOM().getTextContent());

        Optional<String> placeOfBirth = attributes.stream()
            .filter(a -> a.getName().equals(PLACE_OF_BIRTH))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(av -> av.getDOM().getTextContent());

        Optional<String> gender = attributes.stream()
            .filter(a -> a.getName().equals(GENDER))
            .findFirst()
            .map(Attribute::getAttributeValues)
            .map(vs -> vs.get(0))
            .map(v -> ((Gender) v))
            .map(Gender::getValue);

        return new EidasMatchingDataset(null,
                                        dob.orElse(null),
                                        givenName.orElse(null),
                                        familyName.orElse(null),
                                        birthName.orElse(null),
                                        gender.orElse(null),
                                        placeOfBirth.orElse(null)
        );
    }
}
