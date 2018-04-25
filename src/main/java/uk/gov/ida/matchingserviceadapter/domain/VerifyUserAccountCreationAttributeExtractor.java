package uk.gov.ida.matchingserviceadapter.domain;

import com.google.inject.Inject;

public class VerifyUserAccountCreationAttributeExtractor extends UserAccountCreationAttributeExtractor {

    @Inject
    public VerifyUserAccountCreationAttributeExtractor() {
    }

    @Override
    UserAccountCreationAttribute getAttributeExtractor(String name) {
         return UserAccountCreationAttribute.getUserAccountCreationAttribute(name);
    }
}
