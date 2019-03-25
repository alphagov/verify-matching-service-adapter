package uk.gov.ida.matchingserviceadapter.validators;

import uk.gov.ida.saml.core.domain.TransliterableMdsValue;

import java.util.Comparator;

public class FirstNameToComparator implements Comparator<TransliterableMdsValue> {
    @Override
    public int compare(TransliterableMdsValue o1, TransliterableMdsValue o2) {
        if (o1.getTo() == null && o2.getTo() != null) {
            return -1;
        }
        if (o2.getTo() == null && o1.getTo() != null) {
            return 1;
        }
        return 0;
    }
}
