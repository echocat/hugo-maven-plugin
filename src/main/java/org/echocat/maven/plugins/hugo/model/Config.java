package org.echocat.maven.plugins.hugo.model;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.compress.utils.FileNameUtils.getBaseName;
import static org.echocat.maven.plugins.hugo.model.Config.Type.directory;
import static org.echocat.maven.plugins.hugo.model.Config.Type.file;

import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

import org.apache.maven.plugin.MojoFailureException;

public final class Config {

    @Nonnull
    public static Config configOf(@Nonnull Path path) throws MojoFailureException {
        requireNonNull(path);
        return new Config(path);
    }

    @Nonnull
    private final Type type;
    @Nonnull
    private final Path path;
    @Nonnull
    private final String name;

    private Config(@Nonnull Path path) throws MojoFailureException {
        this.path = requireNonNull(path);
        final String fileName = this.path.getFileName().toString();

        if (!exists(this.path)) {
            throw new MojoFailureException(format("Configuration %s does not exist.", this.path));
        } else if (isDirectory(this.path)) {
            this.type = directory;
            this.name = fileName;
        } else if (isRegularFile(this.path)) {
            this.type = file;
            this.name = getBaseName(fileName); // Without extension
        } else {
            throw new MojoFailureException(format("Configuration %s is neither a directory nor regular file.", this.path));
        }
    }

    @Nonnull
    public Type type() {
        return type;
    }

    @Nonnull
    public String parameterName() {
        return type().parameterName();
    }

    @Nonnull
    public Path path() {
        return path;
    }

    @Nonnull
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return path().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        final Config that = (Config) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public enum Type {
        file("--config"),
        directory("--configDir");

        @Nonnull
        private final String parameterName;

        Type(@Nonnull String parameterName) {
            this.parameterName = parameterName;
        }

        @Nonnull
        public String parameterName() {
            return parameterName;
        }
    }
}
