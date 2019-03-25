package uk.gov.ida.matchingserviceadapter.validators;

import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.Comparator;

public class FirstNameVerifiedComparator implements Comparator<TransliterableMdsValue> {
    @Override
    public int compare(TransliterableMdsValue o1, TransliterableMdsValue o2) {
        if (o1.isVerified() == o2.isVerified()) {
            return 0;
        }
        if (o1.isVerified() && !o2.isVerified()) {
            return -1;
        }
        return 1;
    }
}
