package org.echocat.maven.plugins.hugo.utils;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.*;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static org.echocat.maven.plugins.hugo.utils.FileSystems.createParentsOf;
import static org.echocat.maven.plugins.hugo.utils.FileSystems.lastModifiedAt;
import static org.echocat.maven.plugins.hugo.utils.Urls.readUrlFullyToJsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;

public final class HugoVersionRetriever {

    @Nonnull
    private static final URL latestApiUrl = Urls.parse("https://api.github.com/repos/gohugoio/hugo/releases/latest");

    @Nonnull
    public static HugoVersionRetriever.Builder hugoVersionRetriever() {
        return new Builder();
    }

    @Nonnull
    private final Log log;
    @Nonnull
    private final Optional<Duration> latestCacheDuration;
    @Nonnull
    private final Path latestCacheFile;

    private HugoVersionRetriever(@Nonnull Builder builder) {
        log = builder.log.orElseThrow(() -> new NullPointerException("No log provided."));
        latestCacheDuration = builder.latestCacheDuration;
        latestCacheFile = builder.latestCacheFile.orElseGet(HugoVersionRetriever::defaultLatestCacheFile);
    }

    @Nonnull
    public Version latest() throws UncheckedIOException, FailureException {
        return latest(latestApiUrl);
    }

    @Nonnull
    Version latest(@Nonnull URL latestApiUrl) throws UncheckedIOException, FailureException {
        log().debug("Retrieve latest hugo version...");

        final Optional<Version> cached = readLatestCached();
        if (cached.isPresent()) {
            final Version result = cached.get();
            log().debug(format("Latest hugo version retrieved from cache: %s", result));
            return result;
        }

        final JSONObject obj = readUrlFullyToJsonObject(latestApiUrl);
        final Version result = Optional.ofNullable(obj.getString("tag_name"))
            .map(String::trim)
            .filter(v -> !v.isEmpty())
            .map(v -> v.startsWith("v") ? v.substring(1) : v)
            .map(v -> {
                try {
                    return Version.parse(v);
                } catch (ParseException | IllegalArgumentException e) {
                    throw new FailureException(format("'%s' does not contain a valid semantic version.", latestApiUrl), e);
                }
            })
            .orElseThrow(() -> new FailureException(format("'%s' does not contain a valid version name.", latestApiUrl)));

        log().info(format("Latest hugo version retrieved: %s", result));

        try {
            writeLatestCached(result);
        } catch (UncheckedIOException e) {
            log().warn("Cannot store latest hugo version (%s); this will be ignored for now.", e);
        }

        return result;
    }

    @Nonnull
    public Log log() {
        return log;
    }

    @Nonnull
    public Optional<Duration> latestCacheDuration() {
        return latestCacheDuration;
    }

    @Nonnull
    public Path latestCacheFile() {
        return latestCacheFile;
    }

    void writeLatestCached(@Nonnull Version v) throws UncheckedIOException {
        if (!latestCacheDuration.isPresent()) {
            return;
        }

        try {
            createParentsOf(latestCacheFile);
            write(latestCacheFile, singletonList(v.toString()), UTF_8, TRUNCATE_EXISTING, WRITE, CREATE);
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException(format("Cannot write cached hugo version to '%s'.", latestCacheFile), e.getCause());
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot write cached hugo version to '%s'.", latestCacheFile), e);
        }
    }

    @Nonnull
    Optional<Version> readLatestCached() throws UncheckedIOException {
        if (!latestCacheDuration.isPresent()) {
            return Optional.empty();
        }
        try {
            final boolean upToDate = latestCacheDuration.flatMap(v ->
                lastModifiedAt(latestCacheFile).map(lmt -> {
                    final Duration notModifiedSince = ofMillis(currentTimeMillis() - lmt.toMillis());
                    return notModifiedSince.compareTo(v) < 0;
                })
            ).orElse(false);

            if (!upToDate) {
                return Optional.empty();
            }

            final List<String> plain = Files.readAllLines(latestCacheFile, UTF_8);
            if (plain.isEmpty()) {
                return Optional.empty();
            }
            return Version.tryParse(plain.get(0).trim());
        } catch (FileNotFoundException | NoSuchFileException ignored) {
            return Optional.empty();
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException(format("Cannot read cached hugo version from '%s'.", latestCacheFile), e.getCause());
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot read cached hugo version from '%s'.", latestCacheFile), e);
        }
    }

    @Nonnull
    private static Path defaultLatestCacheFile() {
        return Paths.get(getProperty("java.io.tmpdir", "var/tmp"))
            .resolve("hugo_cache")
            .resolve("latest_version");
    }

    public static final class Builder {

        @Nonnull
        private Optional<Log> log = Optional.empty();
        @Nonnull
        private Optional<Duration> latestCacheDuration = Optional.of(Duration.ofHours(1));
        @Nonnull
        private Optional<Path> latestCacheFile = Optional.empty();

        @Nonnull
        public Builder withLog(@Nonnull Log v) {
            log = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withLatestCacheDuration(@Nullable Duration v) {
            latestCacheDuration = Optional.ofNullable(v).filter(d -> !d.isZero() && !d.isNegative());
            return this;
        }

        @Nonnull
        public Builder withLatestCacheFile(@Nonnull Path v) {
            latestCacheFile = Optional.of(v);
            return this;
        }

        @Nonnull
        public HugoVersionRetriever build() {
            return new HugoVersionRetriever(this);
        }

    }
}
