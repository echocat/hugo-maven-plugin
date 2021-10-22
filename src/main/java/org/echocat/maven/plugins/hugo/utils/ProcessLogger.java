package org.echocat.maven.plugins.hugo.utils;

import static org.echocat.maven.plugins.hugo.utils.InputStreamLogger.inputStreamLogger;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.logging.Log;
import org.echocat.maven.plugins.hugo.utils.InputStreamLogger.Level;

public final class ProcessLogger {

    @Nonnull
    public static ProcessLogger.Builder processLogger() {
        return new Builder();
    }

    @Nonnull
    private final Optional<InputStreamLogger> stdout;
    @Nonnull
    private final Optional<InputStreamLogger> stderr;

    private ProcessLogger(@Nonnull Builder builder) {
        final String baseName = builder.name.orElseThrow(() -> new NullPointerException("No name provided."));
        final Log log = builder.log.orElseThrow(() -> new NullPointerException("No log provided."));
        final Process process = builder.process.orElseThrow(() -> new NullPointerException("No process provided."));

        stdout = builder.stdoutLevel
            .map(level -> inputStreamLogger()
                .withName(baseName + ".stdout")
                .withLog(log)
                .withLevel(level)
                .withStdoutOf(process)
                .build()
            );
        stderr = builder.stderrLevel
            .map(level -> inputStreamLogger()
                .withName(baseName + ".stderr")
                .withLog(log)
                .withLevel(level)
                .withStderrOf(process)
                .build()
            );
    }

    public void waitFor() throws InterruptedException {
        if (stdout.isPresent()) {
            stdout.get().waitFor();
        }
        if (stderr.isPresent()) {
            stderr.get().waitFor();
        }
    }

    public static final class Builder {

        @Nonnull
        private Optional<String> name = Optional.empty();
        @Nonnull
        private Optional<Log> log = Optional.empty();
        @Nonnull
        private Optional<Process> process = Optional.empty();
        @Nonnull
        private Optional<Level> stdoutLevel = Optional.empty();
        @Nonnull
        private Optional<Level> stderrLevel = Optional.empty();

        @Nonnull
        public Builder withName(@Nonnull String v) {
            name = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withLog(@Nonnull Log v) {
            log = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withProcess(@Nonnull Process v) {
            process = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withStdoutLevel(@Nullable Level v) {
            stdoutLevel = Optional.ofNullable(v);
            return this;
        }

        @Nonnull
        public Builder withStderrLevel(@Nullable Level v) {
            stderrLevel = Optional.ofNullable(v);
            return this;
        }

        @Nonnull
        public ProcessLogger build() {
            return new ProcessLogger(this);
        }

    }

}
