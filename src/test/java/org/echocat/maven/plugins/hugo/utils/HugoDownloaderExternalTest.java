package org.echocat.maven.plugins.hugo.utils;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static org.echocat.maven.plugins.hugo.model.Versions.version0x145x0;
import static org.echocat.maven.plugins.hugo.utils.HugoDownloader.hugoDownloader;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.echocat.maven.plugins.hugo.model.Platform;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HugoDownloaderExternalTest {

    @ParameterizedTest
    @MethodSource("platforms")
    void download(@Nonnull Platform platform, @Nonnull @TempDir Path tmpDir) throws Exception {
        final HugoDownloader instance = hugoDownloader()
            .withPlatform(platform)
            .withLog(new SystemStreamLog())
            .build();


        final Path tmpFile = tmpDir.resolve("hugo" + platform.executableExtension().orElse(""));
        assertFalse(exists(tmpFile));

        instance.download(version0x145x0, tmpFile);
        assertTrue(isRegularFile(tmpFile));
    }

    @Nonnull
    static Stream<Arguments> platforms() {
        return Stream.of(Platform.values())
            .filter(v -> v.platformSuffix(version0x145x0).isPresent())
            .map(Arguments::of);
    }

}