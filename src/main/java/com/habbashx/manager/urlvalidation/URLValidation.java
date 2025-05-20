package com.habbashx.manager.urlvalidation;

import org.intellij.lang.annotations.Language;

public class URLValidation {

    /**
     * A regular expression pattern used for validating URLs.
     * This pattern matches URLs with the following components:
     * - Supported protocols: HTTP, HTTPS, and FTP.
     * - Domain names containing alphanumeric characters, dots, and hyphens.
     * - A mandatory top-level domain (e.g., .com, .org) with 2 to 6 characters.
     * - An optional port specification starting with ':' followed by 1 to 5 digits.
     * - An optional path segment starting with '/'.
     */
    @Language(value = "RegExp")
    private static final String URL_REGEX = "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(:[0-9]{1,5})?(/.*)?$";

    /**
     * Validates if the provided string is a valid URL based on a predefined URL_REGEX pattern.
     *
     * @param url the string to be validated as a URL
     * @return true if the string matches the URL_REGEX pattern, otherwise false
     */
    public static boolean isValidURL(String url) {
        return url.matches(URL_REGEX);
    }
}
