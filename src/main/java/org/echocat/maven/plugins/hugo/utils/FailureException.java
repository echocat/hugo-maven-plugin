package org.echocat.maven.plugins.hugo.utils;

import javax.annotation.Nonnull;

public class FailureException extends RuntimeException {

    public FailureException(@Nonnull String message) {
        super(message);
    }

    public FailureException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public FailureException(@Nonnull Throwable cause) {
        this(cause.getMessage(), cause);
    }
}
