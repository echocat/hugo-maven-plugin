package org.echocat.maven.plugins.hugo;

import static java.lang.System.getProperty;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

public enum Architecture {
    i386("x86", "i386", "i486", "i586", "i686"),
    amd64("amd64", "x86_64"),
    arm64("aarch64", "arm64");

    private final Set<String> patternCandidates;

    Architecture(@Nonnull String... patternCandidates) {
        this.patternCandidates = unmodifiableSet(new HashSet<>(Arrays.asList(patternCandidates)));
    }

    @Nonnull
    public Set<String> patternCandidates() {
        return patternCandidates;
    }

    public boolean matchesArchString(@Nonnull String what) {
        final String target = requireNonNull(what).toLowerCase();
        for (final String patternCandidate : patternCandidates) {
            if (target.startsWith(patternCandidate)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static Optional<Architecture> findArchitecture() {
        final String arch = getProperty("os.arch", "unknown");
        return findArchitectureMatchesString(arch);
    }

    @Nonnull
    public static Optional<Architecture> findArchitectureMatchesString(@Nonnull String what) {
        for (final Architecture candidate : values()) {
            if (candidate.matchesArchString(what)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

}
