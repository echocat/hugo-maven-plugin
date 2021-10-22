package org.echocat.maven.plugins.hugo.model;

import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nonnull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public enum Packaging {
    tarGz(".tar.gz") {
        @Override
        protected ArchiveInputStream open(@Nonnull InputStream is) throws IOException {
            return new TarArchiveInputStream(new GZIPInputStream(is));
        }
    },
    zip(".zip") {
        @Override
        protected ArchiveInputStream open(@Nonnull InputStream is) {
            return new ZipArchiveInputStream(is);
        }
    };

    @Nonnull
    private final String extension;

    Packaging(@Nonnull String extension) {
        this.extension = requireNonNull(extension);
    }

    @Nonnull
    public String extension() {
        return extension;
    }

    protected abstract ArchiveInputStream open(@Nonnull InputStream is) throws IOException;

    public void extract(@Nonnull String file, @Nonnull Path from, @Nonnull Path to) throws MojoExecutionException, MojoFailureException {
        try (final InputStream is = newInputStream(from);
             final ArchiveInputStream archive = open(is)
        ) {
            ArchiveEntry next = archive.getNextEntry();
            while (next != null) {
                final String actualFileName = new File(next.getName()).getName();
                if (!next.isDirectory() && actualFileName.equalsIgnoreCase(file)) {
                    copy(archive, to);
                    return;
                }
                next = archive.getNextEntry();
            }
            throw new MojoFailureException(format("%s does not contain expected file %s.", from, file));
        } catch (IOException e) {
            throw new MojoExecutionException(format("Cannot extract %s from %s to %s.", from, from, to), e);
        }
    }

    private void copy(@Nonnull InputStream is, @Nonnull Path to) throws IOException {
        try (final OutputStream os = newOutputStream(to)) {
            IOUtils.copy(is, os);
        }
    }

}
