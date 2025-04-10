package org.echocat.maven.plugins.hugo.model;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Optional.empty;
import static org.echocat.maven.plugins.hugo.model.Architecture.*;
import static org.echocat.maven.plugins.hugo.model.Packaging.tarGz;
import static org.echocat.maven.plugins.hugo.model.Packaging.zip;
import static org.echocat.maven.plugins.hugo.model.Versions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.zafarkhaja.semver.Version;
import org.echocat.maven.plugins.hugo.utils.FailureException;

public enum Platform {
    linux_x86(x86, tarGz, false,
        new Vd(version0x54x0, "Linux-32bit"),
        new Vd(version0x95x0)
    ),
    linux_amd64(amd64, tarGz, true,
        new Vd(version0x54x0, "Linux-64bit"),
        new Vd(version0x103x0, "linux-amd64")
    ),
    linux_arm64(arm64, tarGz, false,
        new Vd(version0x54x0, "Linux-ARM64"),
        new Vd(version0x103x0, "linux-arm64")
    ),
    macos_x86(x86, tarGz, false,
        new Vd(version0x54x0, "macOS-32bit"),
        new Vd(Version.of(0, 75, 0))
    ),
    macos_amd64(amd64, tarGz, true,
        new Vd(version0x54x0, "macOS-64bit"),
        new Vd(version0x102x0, "macOS-universal"),
        new Vd(version0x103x0, "darwin-universal")
    ),
    macos_arm64(arm64, tarGz, true,
        new Vd(Version.of(0, 81, 0), "macOS-ARM64", false),
        new Vd(version0x102x0, "macOS-universal"),
        new Vd(version0x103x0, "darwin-universal")
    ),
    windows_x86(x86, zip, false, ".exe",
        new Vd(version0x54x0, "Windows-32bit"),
        new Vd(version0x95x0)
    ),
    windows_amd64(amd64, zip, true, ".exe",
        new Vd(version0x54x0, "Windows-64bit"),
        new Vd(version0x103x0, "windows-amd64")
    ),
    windows_arm32(arm32, zip, false, ".exe",
        new Vd(Version.of(0, 85, 0), "Windows-ARM"),
        new Vd(version0x101x0)
    ),
    windows_arm64(arm64, zip, false, ".exe",
        new Vd(Version.of(0, 89, 0), "Windows-ARM64"),
        new Vd(version0x103x0, "windows-arm64")
    ),
    ;

    private static class Vd implements Comparable<Vd> {
        @Nonnull
        private final Version fromVersion;
        @Nonnull
        private final Optional<String> platformSuffix;
        @Nonnull
        private final Optional<Boolean> extendedSupported;

        private Vd(@Nonnull Version fromVersion, @Nullable String platformSuffix, @Nullable Boolean extendedSupported) {
            this.fromVersion = fromVersion;
            this.platformSuffix = Optional.ofNullable(platformSuffix);
            this.extendedSupported = Optional.ofNullable(extendedSupported);
        }

        private Vd(@Nonnull Version fromVersion, @Nullable String platformSuffix) {
            this(fromVersion, platformSuffix, null);
        }

        private Vd(@Nonnull Version fromVersion) {
            this(fromVersion, null);
        }

        @Override
        public int compareTo(Vd o) {
            return fromVersion.compareTo(o.fromVersion);
        }
    }

    private final static Optional<Platform> actual = detect();

    @Nonnull
    public static Optional<Platform> tryPlatform() {
        return actual;
    }

    @Nonnull
    public static Platform platform() throws FailureException {
        return tryPlatform()
            .orElseThrow(() -> new FailureException("Unsupported platform/operating-system/architecture."));
    }

    @Nonnull
    private final Architecture architecture;
    @Nonnull
    private final Set<Vd> vds;
    @Nonnull
    private final Packaging packaging;
    @Nonnull
    private final Optional<String> executableExtension;

    private final boolean extendedSupported;

    Platform(
        @Nonnull Architecture architecture,
        @Nonnull Packaging packaging,
        boolean extendedSupported,
        Vd... vds
    ) {
        this(architecture, packaging, extendedSupported, null, vds);
    }

    Platform(
        @Nonnull Architecture architecture,
        @Nonnull Packaging packaging,
        boolean extendedSupported,
        @Nullable String executableExtension,
        Vd... vds
    ) {
        this.architecture = architecture;
        this.packaging = packaging;
        this.extendedSupported = extendedSupported;
        this.executableExtension = Optional.ofNullable(executableExtension);
        this.vds = new TreeSet<>(Arrays.asList(vds));
    }

    @Nonnull
    public Architecture architecture() {
        return architecture;
    }

    @Nonnull
    public Optional<String> platformSuffix(@Nonnull Version version) {
        Optional<String> result = empty();
        for (final Vd vd : vds) {
            if (vd.fromVersion.isLowerThanOrEquivalentTo(version)) {
                result = vd.platformSuffix;
            }
        }
        return result;
    }

    @Nonnull
    public Packaging packaging() {
        return packaging;
    }

    public boolean extendedSupported(@Nonnull Version version) {
        boolean result = extendedSupported;
        for (final Vd vd : vds) {
            if (vd.fromVersion.isLowerThanOrEquivalentTo(version) && vd.extendedSupported.isPresent()) {
                result = vd.extendedSupported.get();
            }
        }
        return result;
    }

    @Nonnull
    public Optional<String> executableExtension() {
        return executableExtension;
    }

    @Nonnull
    public Path hugoExecutable(@Nonnull Version version) {
        return hugoExecutableDirectory(version)
            .resolve(hugoExecutableFileName());
    }

    @Nonnull
    private String hugoExecutableFileName() {
        return "hugo"
            + executableExtension().orElse("");
    }

    @Nonnull
    private Path hugoExecutableDirectory(@Nonnull Version version) {
        return tempDirectory()
            .resolve("hugo_cache")
            .resolve("bin")
            .resolve(format("%s-%s", this, version));
    }

    @Nonnull
    private static Path tempDirectory() {
        return Paths.get(getProperty("java.io.tmpdir", "var/tmp"));
    }

    @Nonnull
    public Optional<URL> packageDownloadUrlFor(@Nonnull Version version) throws IllegalStateException {
        return downloadFileNameFor(version).map(downloadFileName -> {
            try {
                return new URL(format("https://github.com/gohugoio/hugo/releases/download/v%s/%s", version, downloadFileName));
            } catch (MalformedURLException e) {
                throw new IllegalStateException(format("Cannot construct valid URL to download hugo in version '%s', for '%s'.", version, this));
            }
        });
    }

    @Nonnull
    private Optional<String> downloadFileNameFor(@Nonnull Version version) {
        return platformSuffix(version)
            .map(platformSuffix -> {
                final StringBuilder sb = new StringBuilder();
                sb.append("hugo_");
                if (extendedSupported(version)) {
                    sb.append("extended_");
                }
                sb.append(version)
                    .append("_")
                    .append(platformSuffix)
                    .append(packaging().extension());

                return sb.toString();
            });
    }

    @Nonnull
    private static Optional<Platform> detect() {
        final Optional<Architecture> arch = findArchitecture();
        if (!arch.isPresent()) {
            return empty();
        }

        final String os = getProperty("os.name", "unknown").toLowerCase();

        if (os.startsWith("windows")) {
            return detectWindows(arch.get());
        }

        if (os.startsWith("linux")) {
            return detectLinux(arch.get());
        }

        if (os.startsWith("mac")) {
            return detectMacOs(arch.get());
        }

        return empty();
    }

    private static Optional<Platform> detectWindows(@Nonnull Architecture arch) {
        switch (arch) {
            case x86:
                return Optional.of(windows_x86);
            case amd64:
                return Optional.of(windows_amd64);
            case arm32:
                return Optional.of(windows_arm32);
            case arm64:
                return Optional.of(windows_arm64);
            default:
                return empty();
        }
    }

    private static Optional<Platform> detectLinux(@Nonnull Architecture arch) {
        switch (arch) {
            case x86:
                return Optional.of(linux_x86);
            case amd64:
                return Optional.of(linux_amd64);
            case arm64:
                return Optional.of(linux_arm64);
            default:
                return empty();
        }
    }

    private static Optional<Platform> detectMacOs(@Nonnull Architecture arch) {
        switch (arch) {
            case amd64:
                return Optional.of(macos_amd64);
            case arm64:
                return Optional.of(macos_arm64);
            default:
                return empty();
        }
    }

}
