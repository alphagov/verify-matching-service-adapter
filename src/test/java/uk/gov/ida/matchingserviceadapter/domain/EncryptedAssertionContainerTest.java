package uk.gov.ida.matchingserviceadapter.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import uk.gov.ida.matchingserviceadapter.domain.EncryptedAssertionContainer;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(OpenSAMLRunner.class)
public class EncryptedAssertionContainerTest {

    @Test
    public void should_retrieveAssertionsFromAttributeQuery() {
        EncryptedAssertion encryptedAssertion = anAssertion().build();
        Assertion unencryptedAssertion = anAssertion().buildUnencrypted();
        AttributeQuery attributeQuery = anAttributeQuery().withSubject(aSubject().withSubjectConfirmation(
                aSubjectConfirmation().withSubjectConfirmationData(aSubjectConfirmationData()
                        .addAssertion(encryptedAssertion)
                        .addAssertion(unencryptedAssertion)
                        .build()).build()).build()).build();

        EncryptedAssertionContainer encryptedAssertionContainer = new EncryptedAssertionContainer(attributeQuery);

        assertThat(encryptedAssertionContainer.getEncryptedAssertions()).containsOnly(encryptedAssertion);
    }
}
