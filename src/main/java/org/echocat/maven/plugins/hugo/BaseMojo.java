package org.echocat.maven.plugins.hugo;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static org.echocat.maven.plugins.hugo.model.Platform.platform;
import static org.echocat.maven.plugins.hugo.utils.Hugo.Download.onDemand;
import static org.echocat.maven.plugins.hugo.utils.HugoVersionRetriever.hugoVersionRetriever;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.zafarkhaja.semver.Version;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.echocat.maven.plugins.hugo.utils.FailureException;
import org.echocat.maven.plugins.hugo.utils.Hugo;
import org.echocat.maven.plugins.hugo.utils.Hugo.Download;
import org.echocat.maven.plugins.hugo.utils.HugoVersionRetriever;
import org.echocat.maven.plugins.hugo.utils.HugoVersionRetriever.Builder;

public abstract class BaseMojo extends AbstractMojo {

    private static final String LATEST_VERSION = "latest";

    @Parameter(
        defaultValue = "${project}",
        readonly = true
    )
    private MavenProject project;

    @Parameter(
        name = "version",
        property = "hugo.version",
        defaultValue = LATEST_VERSION
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

    @Parameter(
        name = "latestVersionCacheDuration",
        property = "hugo.latestVersionCacheDuration"
    )
    private String latestVersionCacheDuration;

    @Parameter(
        name = "latestVersionCacheFile",
        property = "hugo.latestVersionCacheFile"
    )
    private File latestVersionCacheFile;

    @Nonnull
    protected Hugo hugo() throws FailureException {
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
    protected Log log() throws FailureException {
        return ofNullable(getLog())
            .orElseThrow(() -> new FailureException("No log available."));
    }

    @Nonnull
    protected Version version() throws FailureException {
        String plain = Optional.ofNullable(this.version)
            .map(String::trim)
            .filter(v -> !v.isEmpty())
            .orElse(LATEST_VERSION);

        if (LATEST_VERSION.equalsIgnoreCase(plain)) {
            return versionRetriever().latest();
        }

        try {
            return Version.parse(plain);
        } catch (IllegalArgumentException ignored) {
            throw new FailureException(format("Hugo version '%s' is not a valid semantic version.", plain));
        }
    }

    @Nonnull
    protected HugoVersionRetriever versionRetriever() throws FailureException {
        final Builder builder = hugoVersionRetriever()
            .withLog(log());
        if (latestVersionCacheDuration != null && !latestVersionCacheDuration.isEmpty()) {
            builder.withLatestCacheDuration(Duration.parse(latestVersionCacheDuration));
        }
        if (latestVersionCacheFile != null) {
            builder.withLatestCacheFile(latestVersionCacheFile.toPath());
        }

        return builder.build();
    }

    @Nonnull
    protected Download download() {
        return ofNullable(download)
            .orElse(onDemand);
    }

    @Nonnull
    protected Path workingDirectory() throws FailureException {
        return ofNullable(workingDirectory)
            .map(File::toPath)
            .orElseThrow(() -> new FailureException("workingDirectory property missing."));
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
