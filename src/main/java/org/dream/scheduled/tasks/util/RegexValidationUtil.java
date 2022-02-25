package org.dream.scheduled.tasks.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidationUtil {
    
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =  Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private RegexValidationUtil() {}
    
    public static boolean validateEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }
    
    public static boolean validateEmails(List<String> emails) {
        for(String email:emails) {
            if(!validateEmail(email)) {
                return false;
            }
        }
        return true;
    }
}
