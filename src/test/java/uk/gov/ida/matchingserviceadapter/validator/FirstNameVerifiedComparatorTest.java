package uk.gov.ida.matchingserviceadapter.validator;

import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameVerifiedComparator;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FirstNameVerifiedComparatorTest {

    @Test
    public void testVerifiedComesFirstAndOrderPreserved() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("Fred", false));
        firstNames.add(buildFirstName("George", true));
        firstNames.add(buildFirstName("Ron", false));
        firstNames.add(buildFirstName("Charlie", true));
        firstNames.sort(new FirstNameVerifiedComparator());
        assertEquals(firstNames.get(0).getValue(), "George");
        assertEquals(firstNames.get(1).getValue(), "Charlie");
        assertEquals(firstNames.get(2).getValue(), "Fred");
        assertEquals(firstNames.get(3).getValue(), "Ron");
    }

    public TransliterableMdsValue buildFirstName(String name, boolean verified) {
        SimpleMdsValue<String> simpleValue = new SimpleMdsValue<>(name, null, null, verified);
        return new TransliterableMdsValue(simpleValue);
    }
}
