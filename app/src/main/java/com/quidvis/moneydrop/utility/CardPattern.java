package com.quidvis.moneydrop.utility;

public class CardPattern {

    // VISA
    public static final String VISA = "4[0-9]{12}(?:[0-9]{3})?";
    public static final String VISA_MASTER = "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14})$";
    public static final String VISA_VALID = "^4[0-9]{12}(?:[0-9]{3})?$";

    // VERVE
    public static final String VERVE = "((506(0|1))|(507(8|9))|(6500))[0-9]{12,15}";
    public static final String VERVE_VALID = "^((506(0|1))|(507(8|9))|(6500))[0-9]{12,15}$";

    // MasterCard
    public static final String MASTERCARD = "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$";
    public static final String MASTERCARD_SHORT = "^(?:222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)";
    public static final String MASTERCARD_SHORTER = "^(?:5[1-5])";
    public static final String MASTERCARD_VALID = "^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$";

    // Maestro
    public static final String MAESTROCARD = "^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}$";
    public static final String MAESTROCARD_VALID = "^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}$";

    // American Express
    public static final String AMERICAN_EXPRESS = "^3[47][0-9]{0,13}";
    public static final String AMERICAN_EXPRESS_VALID = "^3[47][0-9]{13}$";

    // DISCOVER
    public static final String DISCOVER = "^6(?:011|5[0-9]{1,2})[0-9]{0,12}";
    public static final String DISCOVER_SHORT = "^6(?:011|5)";
    public static final String DISCOVER_VALID = "^6(?:011|5[0-9]{2})[0-9]{12}$";

    // JCB
    public static final String JCB = "^(?:2131|1800|35\\d{0,3})\\d{0,11}$";
    public static final String JCB_SHORT = "^2131|1800";
    public static final String JCB_VALID = "^(?:2131|1800|35\\d{3})\\d{11}$";

    // Discover
    public static final String DINERS_CLUB = "^3(?:0[0-5]|[68][0-9])[0-9]{11}$";
    public static final String DINERS_CLUB_SHORT = "^30[0-5]";
    public static final String DINERS_CLUB_VALID = "^3(?:0[0-5]|[68][0-9])[0-9]{11}$";
}
