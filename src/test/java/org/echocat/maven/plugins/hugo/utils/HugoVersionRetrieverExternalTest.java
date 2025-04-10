package org.echocat.maven.plugins.hugo.utils;

import static java.lang.Thread.sleep;
import static java.nio.file.Files.*;
import static java.time.Duration.ofNanos;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.echocat.maven.plugins.hugo.utils.HugoVersionRetriever.hugoVersionRetriever;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import com.github.zafarkhaja.semver.Version;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HugoVersionRetrieverExternalTest {

    @Test
    void latest_downloads(@TempDir Path tmpDir) throws Exception {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .build();

        assertFalse(isRegularFile(instance.latestCacheFile()));

        final Version actual = instance.latest();
        assertNotNull(actual);
        assertTrue(actual.isHigherThanOrEquivalentTo(Version.of(0, 145, 0)));

        assertTrue(isRegularFile(instance.latestCacheFile()));
        assertEquals(singletonList(actual.toString()), readAllLines(instance.latestCacheFile()));
    }

    @Test
    void latest_useCached(@TempDir Path tmpDir) throws Exception {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .withLatestCacheDuration(ofSeconds(10))
            .build();

        write(instance.latestCacheFile(), "1.2.3".getBytes());
        assertTrue(isRegularFile(instance.latestCacheFile()));

        final Version actual = instance.latest(new URL("https://localhost:6666/does/not/exist"));
        assertNotNull(actual);
        assertEquals(Version.of(1, 2, 3), actual);
    }

    @Test
    void latestCached_nothingCached(@TempDir Path tmpDir) {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .build();

        assertFalse(isRegularFile(instance.latestCacheFile()));
        assertFalse(instance.readLatestCached().isPresent());
    }

    @Test
    void latestCached_tooOld(@TempDir Path tmpDir) throws Exception {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .withLatestCacheDuration(ofNanos(1))
            .build();

        write(instance.latestCacheFile(), "1.2.3".getBytes());
        sleep(10);

        assertTrue(isRegularFile(instance.latestCacheFile()));
        assertFalse(instance.readLatestCached().isPresent());
    }

    @Test
    void latestCached_illegalContent(@TempDir Path tmpDir) throws Exception {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .withLatestCacheDuration(ofSeconds(10))
            .build();

        write(instance.latestCacheFile(), "x1.2.3".getBytes());

        assertTrue(isRegularFile(instance.latestCacheFile()));
        assertFalse(instance.readLatestCached().isPresent());
    }

    @Test
    void latestCached_success(@TempDir Path tmpDir) throws Exception {
        final HugoVersionRetriever instance = hugoVersionRetriever()
            .withLog(new SystemStreamLog())
            .withLatestCacheFile(tmpDir.resolve("latest.cache"))
            .withLatestCacheDuration(ofSeconds(10))
            .build();

        write(instance.latestCacheFile(), "1.2.3".getBytes());

        assertTrue(isRegularFile(instance.latestCacheFile()));
        final Optional<Version> actual = instance.readLatestCached();
        assertTrue(actual.isPresent());
        assertEquals("1.2.3", actual.get().toString());
    }
}