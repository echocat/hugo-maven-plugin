package org.echocat.maven.plugins.hugo.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.echocat.maven.plugins.hugo.utils.InputStreamLogger.Level.info;
import static org.echocat.maven.plugins.hugo.utils.Strings.trimTailingWhitespaces;

import java.io.*;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.logging.Log;

public final class InputStreamLogger {

    @Nonnull
    public static InputStreamLogger.Builder inputStreamLogger() {
        return new Builder();
    }

    @Nonnull
    private final String name;
    @Nonnull
    private final Log log;
    @Nonnull
    private final InputStream input;
    @Nonnull
    private final Level level;

    @Nonnull
    private Optional<Throwable> problem = Optional.empty();
    private boolean running = false;

    private InputStreamLogger(@Nonnull Builder builder) {
        name = builder.name.orElseThrow(() -> new NullPointerException("No name provided."));
        log = builder.log.orElseThrow(() -> new NullPointerException("No log provided."));
        input = builder.input.orElseThrow(() -> new NullPointerException("No input provided."));
        level = builder.level.orElse(info);
    }

    private void start() {
        final Thread thread = new Thread(this::run, name);
        thread.start();
    }

    private void run() {
        Optional<Throwable> problem = Optional.empty();
        synchronized (this) {
            this.running = true;
        }
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(input(), UTF_8));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    log(line);
                }
            } catch (EOFException ignored) {
                // This is ok. It means the upstream is done.
            } catch (Throwable e) {
                problem = Optional.of(e);
            }
        } finally {
            synchronized (this) {
                this.running = false;
                this.problem = problem;
                notifyAll();
            }
        }
    }

    public void waitFor() throws InterruptedException {
        synchronized (this) {
            if (running) {
                wait();
            }
            problem.ifPresent(e -> {
                if (e instanceof Error) {
                    throw (Error) e;
                } if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } if (e instanceof IOException) {
                    throw new UncheckedIOException(e.getMessage(), (IOException) e);
                } else {
                    throw new RuntimeException(e.getMessage(), e);
                }
            });
        }
    }

    private void log(@Nonnull String what) {
        level().log(log(), trimTailingWhitespaces(what));
    }

    @Nonnull
    public Log log() {
        return log;
    }

    @Nonnull
    private InputStream input() {
        return input;
    }

    @Nonnull
    public Level level() {
        return level;
    }

    public static final class Builder {

        @Nonnull
        private Optional<String> name = Optional.empty();
        @Nonnull
        private Optional<Log> log = Optional.empty();
        @Nonnull
        private Optional<InputStream> input = Optional.empty();
        @Nonnull
        private Optional<Level> level = Optional.empty();

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
        public Builder withStdoutOf(@Nonnull Process v) {
            requireNonNull(v);
            return withInput(v.getInputStream());
        }

        @Nonnull
        public Builder withStderrOf(@Nonnull Process v) {
            requireNonNull(v);
            return withInput(v.getErrorStream());
        }

        @Nonnull
        public Builder withInput(@Nonnull InputStream v) {
            input = Optional.of(v);
            return this;
        }

        @Nonnull
        public Builder withLevel(@Nullable Level v) {
            level = Optional.ofNullable(v);
            return this;
        }

        @Nonnull
        public InputStreamLogger build() {
            final InputStreamLogger result = new InputStreamLogger(this);
            result.start();
            return result;
        }
    }

    public enum Level {
        debug(Log::debug),
        info(Log::info),
        warn(Log::warn),
        error(Log::error);

        @Nonnull
        private final BiConsumer<Log, CharSequence> logFunction;

        Level(@Nonnull BiConsumer<Log, CharSequence> logFunction) {
            this.logFunction = logFunction;
        }

        private void log(@Nonnull Log log, @Nonnull CharSequence what) {
            logFunction.accept(log, what);
        }
    }

}
