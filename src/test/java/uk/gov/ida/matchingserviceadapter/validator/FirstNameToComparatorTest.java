package uk.gov.ida.matchingserviceadapter.validator;

import org.joda.time.DateTime;
import org.junit.Test;
import uk.gov.ida.matchingserviceadapter.validators.FirstNameToComparator;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FirstNameToComparatorTest {

    @Test
    public void testNullComesFirstAndOrderPreserved() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("Fred", DateTime.now()));
        firstNames.add(buildFirstName("George", null));
        firstNames.add(buildFirstName("Ron", DateTime.now()));
        firstNames.add(buildFirstName("Charlie", null));
        firstNames.sort(new FirstNameToComparator());
        assertEquals(firstNames.get(0).getValue(), "George");
        assertEquals(firstNames.get(1).getValue(), "Charlie");
        assertEquals(firstNames.get(2).getValue(), "Fred");
        assertEquals(firstNames.get(3).getValue(), "Ron");
    }

    public TransliterableMdsValue buildFirstName(String name, DateTime to) {
        SimpleMdsValue<String> simpleValue = new SimpleMdsValue<>(name, null, to, true);
        return new TransliterableMdsValue(simpleValue);
    }
}
