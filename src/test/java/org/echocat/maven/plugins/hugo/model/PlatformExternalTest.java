package org.echocat.maven.plugins.hugo.model;

import static java.lang.String.format;
import static junit.framework.Assert.assertEquals;
import static org.echocat.maven.plugins.hugo.model.Versions.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PlatformExternalTest {

    @ParameterizedTest
    @MethodSource("versionToPlatform")
    void packageDownloadUrl(@Nonnull Version version, @Nonnull Platform platform) throws Exception {
        final Optional<URL> url = platform.packageDownloadUrlFor(version);
        assertExist(url);
    }

    @Nonnull
    static Stream<Arguments> versionToPlatform() {
        return Stream.of(
            version0x54x0,
            version0x94x0,
            version0x101x0,
            version0x102x0,
            version0x103x0,
            version0x145x0
        ).flatMap(version -> Stream.of(Platform.values()).map(platform ->
            Arguments.of(version, platform)
        ));
    }

    private void assertExist(@Nonnull Optional<URL> url) throws Exception {
        if (!url.isPresent()) {
            return;
        }
        assertExist(url.get());
    }

    private void assertExist(@Nonnull URL url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        final int responseCode = connection.getResponseCode();
        assertEquals(format("Response of HEAD %s should be 200", url), 200, responseCode);
    }

}