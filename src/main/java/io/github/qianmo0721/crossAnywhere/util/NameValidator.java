package io.github.qianmo0721.crossAnywhere.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class NameValidator {
    private static final Pattern ASCII_ALLOWED = Pattern.compile("^[A-Za-z0-9_-]+$");

    private NameValidator() {
    }

    public static String normalize(String name) {
        return name == null ? "" : name.trim();
    }

    public static boolean isValid(String name, int maxLength, boolean allowUnicode) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.length() > maxLength) {
            return false;
        }
        if (!allowUnicode) {
            return ASCII_ALLOWED.matcher(trimmed).matches();
        }
        return true;
    }

    public static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
