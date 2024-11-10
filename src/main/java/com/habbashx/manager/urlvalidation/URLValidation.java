package com.habbashx.manager.urlvalidation;

import org.intellij.lang.annotations.Language;

public class URLValidation {

    @Language(value = "RegExp")
    private static final String URL_REGEX = "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(:[0-9]{1,5})?(/.*)?$";

    public static boolean isValidURL(String url) {
        return url.matches(URL_REGEX);
    }
}
