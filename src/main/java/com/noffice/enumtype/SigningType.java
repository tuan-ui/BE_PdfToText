package com.noffice.enumtype;


public enum SigningType {
    SIGN_CA("_signCA_");
    private final String value;

    SigningType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
