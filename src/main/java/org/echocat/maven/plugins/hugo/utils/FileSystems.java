package org.echocat.maven.plugins.hugo.utils;

import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

public interface FileSystems {

    Set<PosixFilePermission> EXECUTE_PERMISSIONS = unmodifiableSet(new HashSet<>(Arrays.asList(
        OWNER_READ,
        OWNER_WRITE,
        OWNER_EXECUTE,
        GROUP_READ,
        GROUP_EXECUTE,
        OTHERS_READ,
        OTHERS_EXECUTE
    )));

    static void createParentsOf(@Nonnull Path what) throws UncheckedIOException {
        requireNonNull(what);

        createDirectories(what.getParent());
    }

    static void createDirectories(@Nonnull Path what) throws UncheckedIOException {
        requireNonNull(what);
        try {
            Files.createDirectories(what);
        } catch (IOException e) {
            throw new UncheckedIOException(format("%s exists but is not a directory.", what), e);
        }
    }

    static void ensureExecutable(@Nonnull Path what) throws UncheckedIOException {
        try {
            setPosixFilePermissions(what, EXECUTE_PERMISSIONS);
        } catch (UnsupportedOperationException ignored) {
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot make %s executable.", what), e);
        }
    }

    static void rename(@Nonnull Path what, @Nonnull Path to) throws UncheckedIOException {
        try {
            move(what, to, ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot rename %s to %s.", what, to), e);
        }
    }

    @Nonnull
    static Optional<FileTime> lastModifiedAt(@Nonnull Path what) throws UncheckedIOException {
        try {
            return Optional.of(Files.getLastModifiedTime(what));
        } catch (FileNotFoundException | NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
