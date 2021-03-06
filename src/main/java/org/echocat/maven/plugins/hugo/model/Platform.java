package org.echocat.maven.plugins.hugo.model;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Optional.empty;
import static org.echocat.maven.plugins.hugo.model.Architecture.*;
import static org.echocat.maven.plugins.hugo.model.Packaging.tarGz;
import static org.echocat.maven.plugins.hugo.model.Packaging.zip;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public enum Platform {
    linux_x32(x32, "Linux-32bit", tarGz, false),
    linux_x64(x64, "Linux-64bit", tarGz, true),
    linux_arm64(arm64, "Linux-ARM64", tarGz, false),
    macos_x64(x64, "macOS-64bit", tarGz, true),
    macos_arm64(arm64, "macOS-ARM64", tarGz, true),
    windows_x32(x32, "Windows-32bit", zip, false, ".exe"),
    windows_x64(x64, "Windows-64bit", zip, true, ".exe"),
    windows_arm64(arm64, "Windows-ARM", zip, false, ".exe"),
    ;

    private final static Optional<Platform> actual = detect();

    @Nonnull
    public static Optional<Platform> tryPlatform() {
        return actual;
    }

    @Nonnull
    public static Platform platform() throws MojoFailureException {
        return tryPlatform()
            .orElseThrow(() -> new MojoFailureException("Unsupported platform/operating-system/architecture."));
    }

    @Nonnull
    private final Architecture architecture;
    @Nonnull
    private final String platformSuffix;
    @Nonnull
    private final Packaging packaging;
    @Nonnull
    private final Optional<String> executableExtension;

    private final boolean extendedSupported;

    Platform(
        @Nonnull Architecture architecture,
        @Nonnull String platformSuffix,
        @Nonnull Packaging packaging,
        boolean extendedSupported
    ) {
        this(architecture, platformSuffix, packaging, extendedSupported, null);
    }

    Platform(
        @Nonnull Architecture architecture,
        @Nonnull String platformSuffix,
        @Nonnull Packaging packaging,
        boolean extendedSupported,
        @Nullable String executableExtension
    ) {
        this.architecture = architecture;
        this.platformSuffix = platformSuffix;
        this.packaging = packaging;
        this.extendedSupported = extendedSupported;
        this.executableExtension = Optional.ofNullable(executableExtension);
    }

    @Nonnull
    public Architecture architecture() {
        return architecture;
    }

    @Nonnull
    public String platformSuffix() {
        return platformSuffix;
    }

    @Nonnull
    public Packaging packaging() {
        return packaging;
    }

    public boolean extendedSupported() {
        return extendedSupported;
    }

    @Nonnull
    public Optional<String> executableExtension() {
        return executableExtension;
    }

    @Nonnull
    public Path hugoExecutable(@Nonnull String version) {
        return hugoExecutableDirectory(version)
            .resolve(hugoExecutableFileName());
    }

    @Nonnull
    private String hugoExecutableFileName() {
        return "hugo"
            + executableExtension().orElse("");
    }

    @Nonnull
    private Path hugoExecutableDirectory(@Nonnull String version) {
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
    public URL packageDownloadUrlFor(@Nonnull String version) throws MojoExecutionException {
        try {
            return new URL(format("https://github.com/gohugoio/hugo/releases/download/v%s/%s", version, downloadFileNameFor(version)));
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(format("Cannot construct valid URL to download hugo in version '%s', for '%s'.", version, this));
        }
    }

    @Nonnull
    private String downloadFileNameFor(@Nonnull String version) {
        final StringBuilder sb = new StringBuilder();
        sb.append("hugo_");
        if (extendedSupported()) {
            sb.append("extended_");
        }
        sb.append(version)
            .append("_")
            .append(platformSuffix())
            .append(packaging().extension());

        return sb.toString();
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
            case x32:
                return Optional.of(windows_x32);
            case x64:
                return Optional.of(windows_x64);
            case arm64:
                return Optional.of(windows_arm64);
            default:
                return empty();
        }
    }

    private static Optional<Platform> detectLinux(@Nonnull Architecture arch) {
        switch (arch) {
            case x32:
                return Optional.of(linux_x32);
            case x64:
                return Optional.of(linux_x64);
            case arm64:
                return Optional.of(linux_arm64);
            default:
                return empty();
        }
    }

    private static Optional<Platform> detectMacOs(@Nonnull Architecture arch) {
        switch (arch) {
            case x64:
                return Optional.of(macos_x64);
            case arm64:
                return Optional.of(macos_arm64);
            default:
                return empty();
        }
    }

}
