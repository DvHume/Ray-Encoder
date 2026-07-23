package org.quark.ray;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */
public class I18n {
    private static final String BUNDLE_PATH = "lang.messages";
    // Supported languages
    public static final List<Locale> SUPP_LOCALES = List.of(
            Locale.ENGLISH,
            Locale.of("ru"),
            Locale.of("eo")
    );
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        Locale defaultSysLocale = Locale.getDefault();

        // Check if the system lang is supported
        boolean isSupported = SUPP_LOCALES.stream()
                .anyMatch(l -> l.getLanguage().equals(defaultSysLocale.getLanguage()));

        // If the system lang is supported, we take it. Otherwise, Eng is the default
        if (isSupported) {
            setLocale(defaultSysLocale);
        } else { setLocale(Locale.ENGLISH); }
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle(BUNDLE_PATH, currentLocale);
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Get a string by key from a prop file
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * Get a string with argument substitution
     */
    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
