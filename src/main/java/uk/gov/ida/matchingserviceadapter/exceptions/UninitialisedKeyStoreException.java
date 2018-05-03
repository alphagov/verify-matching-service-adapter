package uk.gov.ida.matchingserviceadapter.exceptions;

import java.security.KeyStoreException;

public class UninitialisedKeyStoreException extends RuntimeException {
    public UninitialisedKeyStoreException(KeyStoreException e) {
        super(e);
    }
}
