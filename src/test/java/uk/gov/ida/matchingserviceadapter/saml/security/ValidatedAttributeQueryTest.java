package uk.gov.ida.matchingserviceadapter.saml.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(OpenSAMLRunner.class)
public class ValidatedAttributeQueryTest {

    @Test
    public void should_retrieveAssertionsFromAttributeQuery() {
        EncryptedAssertion assertion = anAssertion().build();
        AttributeQuery attributeQuery = anAttributeQuery().withSubject(aSubject().withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(aSubjectConfirmationData().addAssertion(assertion).build()).build()).build()).build();

        ValidatedAttributeQuery validatedAttributeQuery = new ValidatedAttributeQuery(attributeQuery);

        assertThat(validatedAttributeQuery.getEncryptedAssertions()).contains(assertion);
    }
}
