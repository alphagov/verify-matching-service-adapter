package uk.gov.ida.matchingserviceadapter;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

class ComparatorsTest {

    @Test
    public void comparatorByVerifiedThenCurrent() {
        List<TransliterableMdsValue> firstNames = new ArrayList<>();
        firstNames.add(buildFirstName("historical unverified: expected seventh", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected fifth", null, false));
        firstNames.add(buildFirstName("historical verified: expected third", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected first", null, true));
        firstNames.add(buildFirstName("historical unverified: expected eighth", DateTime.now(), false));
        firstNames.add(buildFirstName("current unverified: expected sixth", null, false));
        firstNames.add(buildFirstName("historical verified: expected fourth", DateTime.now(), true));
        firstNames.add(buildFirstName("current verified: expected second", null, true));
        firstNames.sort(Comparators.comparatorByVerifiedThenCurrent());
        assertEquals("current verified: expected first", firstNames.get(0).getValue());
        assertEquals("current verified: expected second", firstNames.get(1).getValue());
        assertEquals("historical verified: expected third", firstNames.get(2).getValue());
        assertEquals("historical verified: expected fourth", firstNames.get(3).getValue());
        assertEquals("current unverified: expected fifth", firstNames.get(4).getValue());
        assertEquals("current unverified: expected sixth", firstNames.get(5).getValue());
        assertEquals("historical unverified: expected seventh", firstNames.get(6).getValue());
        assertEquals("historical unverified: expected eighth", firstNames.get(7).getValue());
    }

    @Test
    public void attributeComparatorByVerified() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(buildAddress("historical unverified: expected fifth", DateTime.now(), false));
        addresses.add(buildAddress("current unverified: expected sixth", null, false));
        addresses.add(buildAddress("historical verified: expected first", DateTime.now(), true));
        addresses.add(buildAddress("current verified: expected second", null, true));
        addresses.add(buildAddress("historical unverified: expected seventh", DateTime.now(), false));
        addresses.add(buildAddress("current unverified: expected eighth", null, false));
        addresses.add(buildAddress("historical verified: expected third", DateTime.now(), true));
        addresses.add(buildAddress("current verified: expected fourth", null, true));
        addresses.sort(Comparators.attributeComparatorByVerified());
        assertEquals("historical verified: expected first", addresses.get(0).getLines().get(0));
        assertEquals("current verified: expected second", addresses.get(1).getLines().get(0));
        assertEquals("historical verified: expected third", addresses.get(2).getLines().get(0));
        assertEquals("current verified: expected fourth", addresses.get(3).getLines().get(0));
        assertEquals("historical unverified: expected fifth", addresses.get(4).getLines().get(0));
        assertEquals("current unverified: expected sixth", addresses.get(5).getLines().get(0));
        assertEquals("historical unverified: expected seventh", addresses.get(6).getLines().get(0));
        assertEquals("current unverified: expected eighth", addresses.get(7).getLines().get(0));
    }

    private TransliterableMdsValue buildFirstName(String name, DateTime to, boolean verified) {
        SimpleMdsValue<String> simpleValue = new SimpleMdsValue<>(name, null, to, verified);
        return new TransliterableMdsValue(simpleValue);
    }

    private Address buildAddress(String simpleLine, DateTime to, boolean verified) {
        return new Address(Collections.singletonList(simpleLine), "post cod", null, null, null, to, verified);
    }
}
