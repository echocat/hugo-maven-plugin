package org.echocat.maven.plugins.hugo.utils;

import javax.annotation.Nonnull;

public interface Strings {

    @Nonnull
    static String trimTailingWhitespaces(@Nonnull String str) {
        for (int i = str.length() - 1; i >= 0; --i) {
            final char c = str.charAt(i);
            if (c != ' ' && c != '\n' && c != '\r') {
                str = str.substring(0, (i + 1));
                break;
            }
        }
        return str;
    }


}
