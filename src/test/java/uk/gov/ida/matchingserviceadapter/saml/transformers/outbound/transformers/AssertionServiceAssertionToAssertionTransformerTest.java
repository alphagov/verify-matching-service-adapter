package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;
import uk.gov.ida.matchingserviceadapter.saml.factories.UserAccountCreationAttributeFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.impl.VerifiedImpl;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;
import uk.gov.ida.saml.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.builders.MatchingServiceAssertionBuilder.aMatchingServiceAssertion;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.DATE_OF_BIRTH;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.FIRST_NAME;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.MIDDLE_NAME_VERIFIED;

@RunWith(OpenSAMLRunner.class)
public class AssertionServiceAssertionToAssertionTransformerTest {

    private MatchingServiceAssertionToAssertionTransformer transformer;
    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Before
    public void setup() {
        DateTimeFreezer.freezeTime();
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        transformer = new MatchingServiceAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new MatchingServiceAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory));
    }

    @Test
    public void shouldTransformAssertionWithAuthnStatement() {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion().build();
        Assertion assertion = transformer.apply(matchingServiceAssertion);
        assertThat(assertion.getAuthnStatements()).hasSize(1);
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()).isEqualTo(matchingServiceAssertion.getAuthnStatement().getAuthnContext().getUri());
    }

    @Test
    public void shouldNotHaveAttributeStatementIfNoAttributesPresent() {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion()
                .withUserAttributesForAccountCreation(new ArrayList<Attribute>())
                .build();
        Assertion assertion = transformer.apply(matchingServiceAssertion);
        assertThat(assertion.getAttributeStatements()).isEmpty();
    }

    @Test
    public void shouldTransformAssertionsWithFirstNameAttribute() {
        String personName = "John";
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationFirstnameAttribute(
                ImmutableList.of(new SimpleMdsValue<>(personName, null, null, false)));

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(FIRST_NAME, assertion);

        assertThat(((StringValueSamlObject) firstAttributeValue).getValue()).isEqualTo(personName);
    }

    @Test
    public void shouldTransformAssertionsWithMiddleNameVerifiedAttribute() {
        boolean verified = true;
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationVerifiedAttribute(MIDDLE_NAME_VERIFIED, verified);

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(MIDDLE_NAME_VERIFIED, assertion);

        assertThat(((VerifiedImpl) firstAttributeValue).getValue()).isEqualTo(verified);
    }

    @Test
    public void shouldTransformAssertionsWithDateOfBirthAttribute() {
        LocalDate dob = new LocalDate(1980, 10, 30);
        Attribute attribute = new UserAccountCreationAttributeFactory(openSamlXmlObjectFactory).createUserAccountCreationDateOfBirthAttribute(ImmutableList.of(new SimpleMdsValue<>(dob, null, null, false)));

        Assertion assertion = transformAssertionWithAttribute(attribute);
        XMLObject firstAttributeValue = assertAssertionAndGetAttributeValue(DATE_OF_BIRTH, assertion);

        assertThat(((StringValueSamlObject) firstAttributeValue).getValue()).isEqualTo(dob.toString());
    }

    private XMLObject assertAssertionAndGetAttributeValue(final UserAccountCreationAttribute userAccountCreationAttribute, final Assertion assertion) {
        assertThat(assertion.getAttributeStatements()).hasSize(1);
        List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();
        assertThat(attributes).hasSize(1);
        Attribute firstAttribute = attributes.get(0);
        assertThat(firstAttribute.getFriendlyName()).isEqualTo(userAccountCreationAttribute.getAttributeName());
        assertThat(firstAttribute.getAttributeValues()).hasSize(1);

        return firstAttribute.getAttributeValues().get(0);
    }

    private Assertion transformAssertionWithAttribute(final Attribute attribute) {
        MatchingServiceAssertion matchingServiceAssertion = aMatchingServiceAssertion()
                .withUserAttributesForAccountCreation(ImmutableList.of(attribute))
                .build();
        return transformer.apply(matchingServiceAssertion);
    }

}
