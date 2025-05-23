package org.echocat.maven.plugins.hugo.utils;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static org.echocat.maven.plugins.hugo.utils.FileSystems.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;

import com.github.zafarkhaja.semver.Version;
import org.apache.maven.plugin.logging.Log;
import org.echocat.maven.plugins.hugo.model.Platform;

public final class HugoDownloader {

    @Nonnull
    public static HugoDownloader.Builder hugoDownloader() {
        return new Builder();
    }

    @Nonnull
    private final Log log;
    @Nonnull
    private final Platform platform;

    private HugoDownloader(@Nonnull Builder builder) {
        log = builder.log.orElseThrow(() -> new NullPointerException("No log provided."));
        platform = builder.platform.orElseThrow(() -> new NullPointerException("No platform provided."));
    }

    public void download(@Nonnull Version version, @Nonnull Path to) throws UncheckedIOException, FailureException {
        final URL from = platform().packageDownloadUrlFor(version)
            .orElseThrow(() -> new FailureException(format("No binary available for combination of version %s and platform %s.", version, platform)));

        log().info(format("Downloading hugo %s from %s...", version, from));

        final Path packageAsTemporaryFile = downloadToTemporaryFile(from);
        createParentsOf(to);

        final Path temporaryTo = temporaryFor(to);
        platform().packaging().extract(to.getFileName().toString(), packageAsTemporaryFile, temporaryTo);
        ensureExecutable(temporaryTo);
        rename(temporaryTo, to);

        log().info(format("Downloading hugo %s from %s... DONE!", version, from));
    }

    @Nonnull
    Path temporaryFor(@Nonnull Path to) {
        return to.getParent().resolve("~" + to.getFileName());
    }

    @Nonnull
    Path downloadToTemporaryFile(@Nonnull URL from) throws UncheckedIOException {
        final Path to = newBufferFile();
        try (final InputStream is = startDownloadOf(from);
             final OutputStream fileOutputStream = newOutputStream(to)
        ) {
            byte[] dataBuffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(dataBuffer, 0, 4096)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot download hugo and save it into %s.", to), e);
        }
        return to;
    }

    @Nonnull
    Path newBufferFile() throws UncheckedIOException {
        try {
            return createTempFile("hugo-maven-plugin.buffered-download.", "");
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot buffered download file.", e);
        }
    }

    @Nonnull
    InputStream startDownloadOf(@Nonnull URL url) throws UncheckedIOException {
        try {
            return Urls.startDownloadOf(url);
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException(
                format(
                    "Cannot download hugo from %s. [os.name: %s, arch.name: %s]",
                    url,
                    getProperty("os.name", "unknown"),
                    getProperty("os.arch", "unknown")
                ), e.getCause());
        }
    }

    @Nonnull
    public Log log() {
        return log;
    }

    @Nonnull
    public Platform platform() {
        return platform;
    }

    public static final class Builder {

        @Nonnull
        private Optional<Log> log = Optional.empty();
        @Nonnull
        private Optional<Platform> platform = Optional.empty();

        @Nonnull
        public Builder withLog(@Nonnull Log v) {
            log = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withPlatform(@Nonnull Platform v) {
            platform = Optional.of(v);
            return this;
        }

        @Nonnull
        public HugoDownloader build() {
            return new HugoDownloader(this);
        }

    }

}
