package uk.gov.ida.matchingserviceadapter.rest.matchingservice;

import org.joda.time.LocalDate;

public class EidasMatchingDataset {
    private final Address address;
    private final LocalDate dateOfBirth;
    private final String firstName;
    private final String gender;
    private final String familyName;
    private final String birthName;
    private final String placeOfBirth;

    public EidasMatchingDataset(Address address, LocalDate dateOfBirth, String firstName, String familyName, String birthName, String gender, String placeOfBirth) {
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.gender = gender;
        this.familyName = familyName;
        this.birthName = birthName;
        this.placeOfBirth = placeOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthName() {
        return birthName;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public static class Address {
        private final String poBox;
        private final String locatorDesignator;
        private final String locatorName;
        private final String cvAddressArea;
        private final String thoroughFare;
        private final String postName;
        private final String adminunitFirstLine;
        private final String adminunitSecondLine;
        private final String postCode;

        public Address(String poBox, String locatorDesignator, String locatorName, String cvAddressArea, String throroughFare, String postName, String adminunitFirstLine, String adminunitSecondLine, String postCode) {
            this.poBox = poBox;
            this.locatorDesignator = locatorDesignator;
            this.locatorName = locatorName;
            this.cvAddressArea = cvAddressArea;
            this.thoroughFare = throroughFare;
            this.postName = postName;
            this.adminunitFirstLine = adminunitFirstLine;
            this.adminunitSecondLine = adminunitSecondLine;
            this.postCode = postCode;
        }

        public String getPoBox() {
            return poBox;
        }

        public String getLocatorDesignator() {
            return locatorDesignator;
        }

        public String getLocatorName() {
            return locatorName;
        }

        public String getCvAddressArea() {
            return cvAddressArea;
        }

        public String getThoroughFare() {
            return thoroughFare;
        }

        public String getPostName() {
            return postName;
        }

        public String getAdminunitFirstLine() {
            return adminunitFirstLine;
        }

        public String getAdminunitSecondLine() {
            return adminunitSecondLine;
        }

        public String getPostCode() {
            return postCode;
        }
    }
}
