package org.echocat.maven.plugins.hugo.utils;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.lang.String.format;
import static java.nio.file.Files.isExecutable;
import static java.util.Collections.unmodifiableList;
import static org.echocat.maven.plugins.hugo.utils.Hugo.Download.*;
import static org.echocat.maven.plugins.hugo.utils.HugoDownloader.hugoDownloader;
import static org.echocat.maven.plugins.hugo.utils.InputStreamLogger.Level.error;
import static org.echocat.maven.plugins.hugo.utils.InputStreamLogger.Level.info;
import static org.echocat.maven.plugins.hugo.utils.ProcessLogger.processLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.echocat.maven.plugins.hugo.model.Platform;

public final class Hugo {

    @Nonnull
    public static Hugo.Builder hugo() {
        return new Hugo.Builder();
    }

    @Nonnull
    private final Log log;
    @Nonnull
    private final Platform platform;
    @Nonnull
    private final String version;
    @Nonnull
    private final Download download;

    private final HugoDownloader downloader;

    private Hugo(@Nonnull Builder builder) {
        log = builder.log.orElseThrow(() -> new NullPointerException("No log provided."));
        platform = builder.platform.orElseThrow(() -> new NullPointerException("No platform provided."));
        version = builder.version.orElseThrow(() -> new NullPointerException("No version provided."));
        download = builder.download.orElse(onDemand);
        downloader = hugoDownloader()
            .withLog(log())
            .withPlatform(platform())
            .build();
    }

    public void execute(@Nonnull List<String> arguments, @Nonnull Path inWorkingDirectory) throws MojoFailureException, MojoExecutionException {
        final Process process = start(arguments, inWorkingDirectory);
        final ProcessLogger processLogger = processLoggerFor(process);
        try {
            process.waitFor();
            assertNormalExitOf(process);
            processLogger.waitFor();
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Was interrupted.", e);
        }
    }

    @Nonnull
    private Process start(@Nonnull List<String> arguments, @Nonnull Path inWorkingDirectory) throws MojoFailureException, MojoExecutionException {
        final Process process;
        try {
            process = new ProcessBuilder()
                .command(toCommand(arguments))
                .directory(inWorkingDirectory.toFile())
                .redirectError(PIPE)
                .redirectOutput(PIPE)
                .start();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot start hugo process.", e);
        }
        return process;
    }

    @Nonnull
    List<String> toCommand(@Nonnull List<String> arguments) throws MojoFailureException, MojoExecutionException {
        final List<String> result = new ArrayList<>(arguments.size() + 1);
        result.add(executable().toString());
        result.addAll(arguments);
        return unmodifiableList(result);
    }

    @Nonnull
    private ProcessLogger processLoggerFor(@Nonnull Process process) {
        return processLogger()
            .withProcess(process)
            .withName("hugo")
            .withLog(log())
            .withStderrLevel(error)
            .withStdoutLevel(info)
            .build();
    }

    void assertNormalExitOf(@Nonnull Process process) throws MojoFailureException {
        final int value = process.exitValue();
        if (value != 0) {
            throw new MojoFailureException(format("Execution of hugo failed with %d. See output above.", value));
        }
    }

    @Nonnull
    Path executable() throws MojoFailureException, MojoExecutionException {
        final Path result = platform().hugoExecutable(version());
        final Download download = download();

        if (isExecutable(result) && download != always) {
            return result;
        }

        if (download == never) {
            throw new MojoFailureException("hugo is not available but should also never be downloaded.");
        }

        downloader.download(version(), result);

        return result;
    }

    @Nonnull
    public Log log() {
        return log;
    }

    @Nonnull
    public Platform platform() {
        return platform;
    }

    @Nonnull
    public String version() {
        return version;
    }

    @Nonnull
    public Download download() {
        return download;
    }

    public static final class Builder {

        @Nonnull
        private Optional<Log> log = Optional.empty();
        @Nonnull
        private Optional<Platform> platform = Optional.empty();
        @Nonnull
        private Optional<String> version = Optional.empty();
        @Nonnull
        private Optional<Download> download = Optional.empty();

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
        public Builder withVersion(@Nonnull String v) {
            version = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withDownload(@Nullable Download v) {
            download = Optional.ofNullable(v);
            return this;
        }

        @Nonnull
        public Hugo build() {
            return new Hugo(this);
        }

    }

    public enum Download {
        never,
        onDemand,
        always
    }

}
