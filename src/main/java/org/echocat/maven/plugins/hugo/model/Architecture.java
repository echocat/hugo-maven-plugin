package org.echocat.maven.plugins.hugo.model;

import static java.lang.System.getProperty;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public enum Architecture {
    amd64("^(x8664|amd64|ia32e|em64t|x64)$"),
    x86("^(x8632|x86|i[3-6]86|ia32|x32)$"),
    arm32("^(arm|arm32)$"),
    arm64("^(aarch64|arm64)$"),
    ;

    private final Pattern pattern;

    Architecture(@Nonnull String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matchesArchString(@Nonnull String what) {
        String normalizedWhat = requireNonNull(what).toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
        return pattern.matcher(normalizedWhat).matches();
    }

    @Nonnull
    public static Optional<Architecture> findArchitecture() {
        final String arch = ArchNameProvider.get();
        return findArchitectureMatchesString(arch);
    }

    @Nonnull
    public static Optional<Architecture> findArchitectureMatchesString(@Nonnull String what) {
        return Arrays
            .stream(values())
            .filter(architecture -> architecture.matchesArchString(what))
            .findFirst();
    }

    static class ArchNameProvider {
        static String get() {
            return getProperty("os.arch", "unknown");
        }
    }

}
