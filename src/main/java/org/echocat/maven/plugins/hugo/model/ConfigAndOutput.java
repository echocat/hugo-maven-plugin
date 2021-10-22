package org.echocat.maven.plugins.hugo.model;

import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class ConfigAndOutput {

    @Nonnull
    public static ConfigAndOutput configAndOutputOf(
        @Nonnull Config config,
        @Nonnull Path output
    ) {
        return new ConfigAndOutput(config, output);
    }

    @Nonnull
    private final Config config;
    @Nonnull
    private final Path output;

    private ConfigAndOutput(
        @Nonnull Config config,
        @Nonnull Path output
    ) {
        this.config = config;
        this.output = output;
    }

    @Nonnull
    public Config config() {
        return config;
    }

    @Nonnull
    public Path output() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        final ConfigAndOutput that = (ConfigAndOutput) o;
        return config.equals(that.config) && output.equals(that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, output);
    }

    @Override
    public String toString() {
        return config() + " -> " + output();
    }
}
