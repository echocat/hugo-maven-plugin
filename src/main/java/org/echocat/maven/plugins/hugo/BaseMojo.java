package org.echocat.maven.plugins.hugo;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static org.echocat.maven.plugins.hugo.utils.Hugo.Download.onDemand;
import static org.echocat.maven.plugins.hugo.model.Platform.platform;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.echocat.maven.plugins.hugo.utils.Hugo;
import org.echocat.maven.plugins.hugo.utils.Hugo.Download;

public abstract class BaseMojo extends AbstractMojo {

    private static final String DEFAULT_VERSION = "0.88.1";

    @Parameter(
        defaultValue = "${project}",
        readonly = true
    )
    private MavenProject project;

    @Parameter(
        name = "version",
        property = "hugo.version",
        defaultValue = DEFAULT_VERSION
    )
    private String version;

    @Parameter(
        name = "download",
        property = "hugo.download",
        defaultValue = "onDemand"
    )
    private Download download;

    @Parameter(
        name = "workingDirectory",
        property = "hugo.workingDirectory",
        defaultValue = "${project.basedir}"
    )
    private File workingDirectory;

    @Parameter(
        name = "additionalArguments"
    )
    private List<String> additionalArguments;

    @Parameter(
        name = "outputIncludes"
    )
    private List<String> outputIncludes;

    @Parameter(
        name = "outputExcludes"
    )
    private List<String> outputExcludes;

    @Parameter(
        name = "environment",
        property = "hugo.environment"
    )
    private String environment;

    @Nonnull
    protected Hugo hugo() throws MojoFailureException {
        return Hugo.hugo()
            .withLog(log())
            .withVersion(version())
            .withDownload(download())
            .withPlatform(platform())
            .build()
            ;
    }

    @Nonnull
    protected Optional<MavenProject> project() {
        return ofNullable(project);
    }

    @Nonnull
    protected Log log() throws MojoFailureException {
        return ofNullable(getLog())
            .orElseThrow(() -> new MojoFailureException("No log available."));
    }

    @Nonnull
    protected String version() {
        return ofNullable(version)
            .orElse(DEFAULT_VERSION);
    }

    @Nonnull
    protected Download download() {
        return ofNullable(download)
            .orElse(onDemand);
    }

    @Nonnull
    protected Path workingDirectory() throws MojoFailureException {
        return ofNullable(workingDirectory)
            .map(File::toPath)
            .orElseThrow(() -> new MojoFailureException("workingDirectory property missing."));
    }


    @Nonnull
    protected List<String> arguments(@Nullable String... args) {
        return arguments(args != null ? asList(args) : emptyList());
    }

    @Nonnull
    protected List<String> arguments(@Nonnull List<String> input) {
        final List<String> result = new ArrayList<>(input);

        environment().ifPresent(v -> {
            result.add("--environment");
            result.add(v);
        });

        ofNullable(additionalArguments)
            .ifPresent(result::addAll);
        return unmodifiableList(result);
    }

    @Nonnull
    protected Resource toOutputResource(@Nonnull Path path, @Nonnull String target) {
        final Resource result = new Resource();
        result.setTargetPath(target);
        result.setDirectory(path.toString());
        result.setIncludes(outputIncludes);
        result.setExcludes(outputExcludes);
        return result;
    }

    @Nonnull
    protected Optional<String> environment() {
        return ofNullable(environment);
    }


}
