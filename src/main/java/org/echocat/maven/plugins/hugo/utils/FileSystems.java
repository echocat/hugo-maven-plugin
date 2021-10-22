package org.echocat.maven.plugins.hugo.utils;

import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoExecutionException;

public final class FileSystems {

    public static final Set<PosixFilePermission> EXECUTE_PERMISSIONS = unmodifiableSet(new HashSet<>(Arrays.asList(
        OWNER_READ,
        OWNER_WRITE,
        OWNER_EXECUTE,
        GROUP_READ,
        GROUP_EXECUTE,
        OTHERS_READ,
        OTHERS_EXECUTE
    )));

    public static void createParentsOf(@Nonnull Path what) throws MojoExecutionException {
        requireNonNull(what);

        createDirectories(what.getParent());
    }

    public static void createDirectories(@Nonnull Path what) throws MojoExecutionException {
        requireNonNull(what);
        try {
            Files.createDirectories(what);
        } catch (IOException e) {
            throw new MojoExecutionException(format("%s exists but is not a directory.", what), e);
        }
    }

    public static void ensureExecutable(@Nonnull Path what) throws MojoExecutionException {
        try {
            setPosixFilePermissions(what, EXECUTE_PERMISSIONS);
        } catch (UnsupportedOperationException ignored) {
        } catch (IOException e) {
            throw new MojoExecutionException(format("Cannot make %s executable.", what), e);
        }
    }

    public static void rename(@Nonnull Path what, @Nonnull Path to) throws MojoExecutionException {
        try {
            move(what, to, ATOMIC_MOVE);
        } catch (IOException e) {
            throw new MojoExecutionException(format("Cannot rename %s to %s.", what, to), e);
        }
    }

    private FileSystems() {}

}
